#!/usr/bin/env python3

"""
CPAchecker is a tool for configurable software verification.
This file is part of CPAchecker.

Copyright (C) 2007-2014  Dirk Beyer
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


CPAchecker web page:
  http://cpachecker.sosy-lab.org
"""

import sys
import os
import glob
import subprocess
import argparse

import logging
import re

"""
CPA-Witness-Exec module for validating witness files by using a generate and validate approach.
Creates a test harness based on the violation witness given for an input file,
compiles the file with the created harness and checks whether the created program
reaches the target location specified by the violation witness.

Currently, only reachability properties are supported.
"""

__version__ = '0.1'


GCC_ARGS_FIXED = ["-D__alias__(x)="]
EXPECTED_RETURN = 107

MACHINE_MODEL_32 = '32bit'
MACHINE_MODEL_64 = '64bit'

HARNESS_EXE_NAME = 'test_suite'

ARG_PLACEHOLDER = '9949e80c0b01459493a90a2a5646ffbc'  # Random string generated by uuid.uuid4()


class ValidationError(Exception):
    """Exception representing an validation error."""

    def __init__(self, msg):
        self._msg = msg

    @property
    def msg(self):
        return self._msg


class ExecutionResult(object):
    """Results of a subprocess execution."""

    def __init__(self, returncode, stdout, stderr):
        self._returncode = returncode
        self._stdout = stdout
        self._stderr = stderr

    @property
    def returncode(self):
        return self._returncode

    @property
    def stdout(self):
        return self._stdout

    @property
    def stderr(self):
        return self._stderr


def get_cpachecker_version():
    executable = get_cpachecker_executable()
    result = execute([executable, "-help"], quiet=True)
    for line in result.stdout.split(os.linesep):
        if line.startswith('CPAchecker'):
            return line.replace('CPAchecker', '').strip()
    return None


def create_parser():
    parser = argparse.ArgumentParser(description="Validate a given violation witness for an input file.")

    machine_model_args = parser.add_mutually_exclusive_group(required=False)
    machine_model_args.add_argument('-32',
                                    dest='machine_model', action='store_const', const=MACHINE_MODEL_32,
                                    help="Use 32 bit machine model"
                                    )
    machine_model_args.add_argument('-64',
                                    dest='machine_model', action='store_const', const=MACHINE_MODEL_64,
                                    help="Use 64 bit machine model"
                                    )
    machine_model_args.set_defaults(machine_model=MACHINE_MODEL_32)

    parser.add_argument('--outputpath',
                        dest='output_path',
                        type=str, action='store', default="output",
                        help="Path where output should be stored"
                        )

    parser.add_argument('--timelimit',
                        type=str,
                        action='store',
                        help='Time limit of analysis')

    parser.add_argument('--cpa-args',
                        dest='cpa_args',
                        type=_postprocess_args,
                        action='append',
                        default=[],
                        help='List of arguments to use for CPAchecker when generating the test harnesses. ' +
                             'This should include -generateTestHarness .'
                        )

    parser.add_argument('--gcc-args',
                        dest='gcc_args',
                        type=_postprocess_args,
                        action='append',
                        default=[],
                        help='List of arguments to use when compiling the counterexample test'
                        )

    parser.add_argument("--version", '-v',
                        action="version", version='{}'.format(get_cpachecker_version())
                        )

    parser.add_argument("file",
                        type=str,
                        nargs='+',
                        help="File(s) to validate witness for"
                        )

    return parser


def _preprocess_args(argv):
    """ Preprocess command line parameters.
        Replaces the '-' of the first argument given as parameter to --gcc-args and --cpa-args
        with some placeholder so that argparse does recognize it as a parameter.
        Otherwise, it will look at it as another argument and raise an error.

        Example argv where this happens:
            --gcc-args "-ggdb" --outputpath output example.i

        This misbehavior only appears if a single argument is given, but works otherwise, e.g.:
            --gcc-args "-ggdb -O2" --outputpath output example.i
        """
    new_argv = []
    change_next = False
    for arg in argv:
        new_arg = arg
        if change_next:
            assert arg.startswith('-')
            new_arg = ARG_PLACEHOLDER + arg[1:]
            change_next = False
        elif new_arg == '--gcc-args' or new_arg == '--cpa-args':
            change_next = True

        new_argv.append(new_arg)
    return new_argv


def _postprocess_args(arg):
    """ Undo the preprocessing steps from above. """
    new_arg = arg
    if arg.startswith(ARG_PLACEHOLDER):
        new_arg = '-' + arg[len(ARG_PLACEHOLDER):]
    return new_arg.split()

def _parse_args(argv=sys.argv[1:]):
    parser = create_parser()
    args = parser.parse_args(_preprocess_args(argv))

    return args


def flatten(list_of_lists):
    return sum(list_of_lists, [])


def _create_gcc_basic_args(args):
    gcc_args = GCC_ARGS_FIXED + flatten(args.gcc_args)
    if args.machine_model == MACHINE_MODEL_64:
        gcc_args.append('-m64')
    elif args.machine_model == MACHINE_MODEL_32:
        gcc_args.append('-m32')
    else:
        raise ValidationError('Neither 32 nor 64 bit machine model specified')

    return gcc_args


def _create_gcc_cmd_tail(harness, file, target):
    return ['-o', target, harness] + file


def create_compile_cmd(harness, target, args, c_version='c11'):
    gcc_cmd = ['gcc'] + _create_gcc_basic_args(args)
    gcc_cmd.append('-std={}'.format(c_version))
    gcc_cmd += _create_gcc_cmd_tail(harness, args.file, target)
    return gcc_cmd


def _create_cpachecker_args(args):
    cpachecker_args = flatten(args.cpa_args)

    # An explicit output path that is set using -cpa-args will be respected
    if '-outputpath' not in cpachecker_args:
        cpachecker_args += ["-outputpath", args.output_path]

    if '-timelimit' not in cpachecker_args and args.timelimit is not None:
        cpachecker_args += ["-timelimit", args.timelimit]

    if args.machine_model == MACHINE_MODEL_64:
        cpachecker_args.append('-64')
    elif args.machine_model == MACHINE_MODEL_32:
        cpachecker_args.append('-32')
    else:
        raise ValidationError('Neither 32 nor 64 bit machine model specified')

    cpachecker_args += args.file
    return cpachecker_args


def get_cpachecker_executable():
    executable_name = 'cpa.sh'

    def is_exe(exe_path):
        return os.path.isfile(exe_path) and os.access(exe_path, os.X_OK)

    # Directories the CPAchecker executable may ly in.
    # It's important to put '.' and './scripts' last, because we
    # want to look at the "real" PATH directories first
    path_candidates = os.environ["PATH"].split(os.pathsep) + ['.', '.' + os.sep + 'scripts']
    for path in path_candidates:
        path = path.strip('"')
        exe_file = os.path.join(path, executable_name)
        if is_exe(exe_file):
            return exe_file

    raise ValidationError("CPAchecker executable not found or not executable!")


def create_harness_gen_cmd(args):
    cpa_executable = get_cpachecker_executable()
    harness_gen_args = _create_cpachecker_args(args)
    return [cpa_executable] + harness_gen_args


def find_harnesses(output_path):
    return glob.glob(output_path + "/*harness.c")


def get_target_name(harness_name):
    harness_number = re.search(r'(\d+)\.harness\.c', harness_name).group(1)

    return "test_cex" + harness_number


def execute(command, quiet=False):
    if not quiet:
        logging.info(" ".join(command))
    p = subprocess.Popen(command,
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE,
                         universal_newlines=True
                         )
    returncode = p.wait()
    output = p.stdout.read()
    err_output = p.stderr.read()

    return ExecutionResult(returncode, output, err_output)


def analyze_result(test_result, harness):
    if test_result.returncode == EXPECTED_RETURN:
        print("Verification result: FALSE." +
              " Harness {} reached expected error location.".format(harness))
        return True
    else:  # Only log failures to info level
        logging.info("Run with harness {} was not successful".format(harness))


def log_multiline(msg, level=logging.INFO):
    for line in msg.split('\n'):
        logging.log(level, line) if line else None


def run():
    args = _parse_args()
    output_dir = args.output_path

    harness_gen_cmd = create_harness_gen_cmd(args)
    harness_gen_result = execute(harness_gen_cmd)
    log_multiline(harness_gen_result.stdout, level=logging.DEBUG)
    log_multiline(harness_gen_result.stderr, level=logging.INFO)

    created_harnesses = find_harnesses(output_dir)
    logging.info("{} harness(es) for witness produced.".format(len(created_harnesses)))

    done = False
    for harness in created_harnesses:
        logging.info("Looking at {}".format(harness))
        exe_target = output_dir + os.sep + get_target_name(harness)
        compile_cmd = create_compile_cmd(harness, exe_target, args)
        compile_result = execute(compile_cmd)

        log_multiline(compile_result.stdout, level=logging.DEBUG)
        log_multiline(compile_result.stderr, level=logging.INFO)

        if compile_result.returncode != 0:
            compile_cmd = create_compile_cmd(harness, exe_target, args, 'c90')
            compile_result = execute(compile_cmd)
            log_multiline(compile_result.stdout, level=logging.DEBUG)
            log_multiline(compile_result.stderr, level=logging.INFO)

            if compile_result.returncode != 0:
                logging.warning("Compilation failed for harness {}".format(harness))
                continue

        test_result = execute([exe_target])
        test_stdout_file = output_dir + os.sep + 'stdout.txt'
        test_stderr_file = output_dir + os.sep + 'stderr.txt'
        with open(test_stdout_file, 'w+') as output:
            output.write(test_result.stdout)
        with open(test_stderr_file, 'w+') as error_output:
            error_output.write(test_result.stderr)

        done = analyze_result(test_result, harness)
        if done:
            break

    if not done:
        print("Verification result: UNKNOWN." +
              " No harness for witness was successful or no harness was produced.")


logging.basicConfig(format="%(levelname)s: %(message)s",
                    level=logging.INFO)

try:
    run()
except ValidationError as e:
    logging.error(e.msg)
