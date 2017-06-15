/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2017  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.cfa.parser.llvm;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.List;
import org.llvm.BasicBlock;
import org.llvm.Module;
import org.llvm.Value;
import java.util.SortedMap;
import java.util.TreeMap;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.ast.ADeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CAstNode;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CCharLiteralExpression;
import org.sosy_lab.cpachecker.cfa.ast.ADeclaration;
import org.sosy_lab.cpachecker.cfa.ast.FileLocation;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CReturnStatement;
import org.sosy_lab.cpachecker.cfa.types.c.CBasicType;
import org.sosy_lab.cpachecker.cfa.types.c.CSimpleType;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CLabelNode;
import org.sosy_lab.cpachecker.cfa.model.c.CAssumeEdge;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.FunctionExitNode;
import org.sosy_lab.cpachecker.cfa.model.BlankEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CDeclarationEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CReturnStatementEdge;
import org.sosy_lab.cpachecker.util.Pair;
import java.util.function.Function;
import org.llvm.*;

/**
 * Visitor for the AST generated by our LLVM parser
 *
 * @see LlvmParser
 */
public abstract class LlvmAstVisitor {

  // unnamed basic blocks will be named as 1,2,3,...
  private int basicBlockId;
  protected SortedMap<String, FunctionEntryNode> functions;

  protected SortedSetMultimap<String, CFANode> cfaNodes;
  protected List<Pair<ADeclaration, String>> globalDeclarations;

  private final LogManager logger;

  public LlvmAstVisitor(LogManager pLogger) {
    logger = pLogger;

    basicBlockId = 0;

    functions = new TreeMap<>();
    cfaNodes = TreeMultimap.create();
    globalDeclarations = new ArrayList<>();
  }

  public void visit(final Module pItem) {
    if (pItem.getFirstFunction() == null)
      return;

    addFunctionDeclarations(pItem);

    /* create globals */
    iterateOverGlobals(pItem);

    /* create CFA for all functions */
    iterateOverFunctions(pItem);
  }

  private void addFunctionDeclarations(final Module pItem) {
    for (Value func : pItem) {
      String funcName = func.getValueName();
      assert !funcName.isEmpty();

      // XXX: may just check for generic intrinsic?
      if (funcName.startsWith("llvm."))
        continue;

      declareFunction(func);
    }
  }

  private void iterateOverGlobals(final Module pItem) {
    Value globalItem = pItem.getFirstGlobal();
    /* no globals? */
    if (globalItem == null)
      return;

    Value globalItemLast = pItem.getLastGlobal();
    assert globalItemLast != null;

    while (true) {
      ADeclaration decl = visitGlobalItem(globalItem);
      //globalDeclarations.add(Pair.of(decl, globalItem.toString()));

      /* we processed the last global variable? */
      if (globalItem.equals(globalItemLast))
        break;

      globalItem = globalItem.getNextGlobal();
    }
  }

  protected void addNode(String funcName, CFANode nd) {
      cfaNodes.put(funcName, nd);
  }

  private void addEdge(CFAEdge edge) {
      edge.getPredecessor().addLeavingEdge(edge);
      edge.getSuccessor().addEnteringEdge(edge);
  }

  private void iterateOverFunctions(final Module pItem) {
    for (Value func : pItem) {
      if (func.isDeclaration())
        continue;

      String funcName = func.getValueName();
      assert !funcName.isEmpty();

      // XXX: may just check for generic intrinsic?
      if (funcName.startsWith("llvm."))
        continue;

      // handle the function definition
      FunctionEntryNode en = visitFunction(func);
      assert en != null;
      addNode(funcName, en);

      // create the basic blocks and instructions of the function.
      // A basic block is mapped to a pair <entry node, exit node>
      SortedMap<Long, BasicBlockInfo> basicBlocks = new TreeMap<>();
      CLabelNode entryBB = iterateOverBasicBlocks(func, en, funcName, basicBlocks);

      // add the edge from the entry of the function to the first
      // basic block
      //BlankEdge.buildNoopEdge(en, entryBB);
      addEdge(new BlankEdge("entry", FileLocation.DUMMY,
                             en, entryBB, "Function start edge"));

      // add branching between instructions
      addJumpsBetweenBasicBlocks(func, basicBlocks);

      functions.put(funcName, en);
    }
  }

  /**
   * Iterate over basic blocks of a function.
   *
   * Add a label created for every basic block to a mapping
   * passed as an argument. @return the entry basic block
   * (as a CLabelNode).
   */
  private CLabelNode iterateOverBasicBlocks(final Value pItem,
                                            FunctionEntryNode entryNode,
                                            String funcName,
                                            SortedMap<Long, BasicBlockInfo> basicBlocks) {
    assert pItem.isFunction();
    if (pItem.countBasicBlocks() == 0)
      return null;

    CLabelNode entryBB = null;
    org.llvm.Function F = pItem.asFunction();
    for (BasicBlock BB : F) {
      // process this basic block
      CLabelNode label = new CLabelNode(funcName, getBBName(BB));
      addNode(funcName, label);
      if (entryBB == null)
        entryBB = label;

      BasicBlockInfo bbi = handleInstructions(entryNode.getExitNode(), funcName, BB);
      basicBlocks.put(BB.getAddress(), new BasicBlockInfo(label, bbi.getExitNode()));

      // add an edge from label to the first node
      // of this basic block
      addEdge(new BlankEdge("label_to_first", FileLocation.DUMMY,
                            label, bbi.getEntryNode(), "edge to first instr"));

    }

    assert entryBB != null || basicBlocks.isEmpty();
    return entryBB;
  }

  /**
   * Add branching edges between first and last nodes of basic blocks.
   */
  private void addJumpsBetweenBasicBlocks(final Value pItem,
                                          SortedMap<Long, BasicBlockInfo> basicBlocks) {
    assert pItem.isFunction();

    // for every basic block, get the last instruction and
    // add edges from it to labels where it jumps
    org.llvm.Function F = pItem.asFunction();
    for (BasicBlock BB : F) {
      Value terminatorInst = BB.getLastInstruction();
      if (terminatorInst == null) {
        continue;
      }

      assert terminatorInst.isTerminatorInst();
      CFANode brNode = basicBlocks.get(BB.getAddress()).getExitNode();

      int succNum = terminatorInst.getNumSuccessors();
      if (succNum == 0) {
        continue;
      } else if (succNum == 1) {
        BasicBlock succ = terminatorInst.getSuccessor(0);
        CLabelNode label = (CLabelNode)basicBlocks.get(succ.getAddress()).getEntryNode();

        addEdge(new BlankEdge("(goto)", FileLocation.DUMMY,
                              brNode, (CFANode)label, "(goto)"));
        continue;
      }

      // switch is not supported yet
      assert succNum == 2;

      // get the operands and add branching edges
      terminatorInst.dumpValue();
      CExpression condition = getBranchCondition(terminatorInst, pItem.getValueName());

      BasicBlock succ = terminatorInst.getSuccessor(0);
      CLabelNode label = (CLabelNode)basicBlocks.get(succ.getAddress()).getEntryNode();
      addEdge(new CAssumeEdge(condition.toASTString(), FileLocation.DUMMY,
                              brNode, (CFANode)label, condition, true));

      succ = terminatorInst.getSuccessor(1);
      label = (CLabelNode)basicBlocks.get(succ.getAddress()).getEntryNode();
      addEdge(new CAssumeEdge(condition.toASTString(), FileLocation.DUMMY,
                              brNode, (CFANode)label, condition, false));
    }
  }

  private String getBBName(BasicBlock BB) {
    Value bbValue = BB.basicBlockAsValue();
    String labelStr = bbValue.getValueName();
    if (labelStr.isEmpty()) {
      return Integer.toString(++basicBlockId);
    } else {
      return labelStr;
    }
  }

  private CFANode newNode(String funcName) {
    CFANode nd = new CFANode(funcName);
    addNode(funcName, nd);

    return nd;
  }

  /**
   * Create a chain of nodes and edges corresponding to one basic block.
   */
  private BasicBlockInfo handleInstructions(FunctionExitNode exitNode,
                                            String funcName, final BasicBlock pItem) {
    assert pItem.getFirstInstruction() != null; // empty BB not supported

    Value lastI = pItem.getLastInstruction();
    assert lastI != null;

    CFANode prevNode = newNode(funcName);
    CFANode firstNode = prevNode;
    CFANode curNode = null;

    for (Value I : pItem) {
      if (I.isDbgInfoIntrinsic() || I.isDbgDeclareInst())
        continue;

      // process this basic block
      List<CAstNode> expressions = visitInstruction(I, funcName);
      if (expressions == null) {
        curNode = newNode(funcName);
        addEdge(new BlankEdge(I.toString(), FileLocation.DUMMY,
                             prevNode, curNode, "(noop)"));
        prevNode = curNode;
        continue;
      }

      for (CAstNode expr : expressions) {
        // build an edge with this expression over it
        if (expr instanceof CDeclaration) {
         curNode = newNode(funcName);
         addEdge(new CDeclarationEdge(expr.toASTString(), FileLocation.DUMMY,
                                      prevNode, curNode,
                                      (CDeclaration)expr));
        } else if (expr instanceof CReturnStatement) {
          curNode = exitNode;
          addEdge(new CReturnStatementEdge(I.toString(), (CReturnStatement)expr,
                                           FileLocation.DUMMY, prevNode, exitNode));
        } else {
          curNode = newNode(funcName);
          addEdge(new CStatementEdge(expr.toASTString() + I.toString(), (CStatement)expr,
                                     FileLocation.DUMMY, prevNode, curNode));
        }

        prevNode = curNode;
      }

      // did we processed all instructions in this basic block?
      if (I.equals(lastI))
        break;
    }

    assert curNode != null;
    return new BasicBlockInfo(firstNode, curNode);
  }

  private static class BasicBlockInfo {
      private CFANode entryNode;
      private CFANode exitNode;

      public BasicBlockInfo(CFANode entry, CFANode exit) {
          entryNode = entry;
          exitNode = exit;
      }

      public CFANode getEntryNode() {
          return entryNode;
      }

      public CFANode getExitNode() {
          return exitNode;
      }
  }

  protected abstract FunctionEntryNode visitFunction(final Value pItem);
  protected abstract void declareFunction(final Value pItem);
  protected abstract List<CAstNode> visitInstruction(Value pItem, String pFunctionName);
  protected abstract CExpression getBranchCondition(Value pItem, String funcName);

  protected abstract ADeclaration visitGlobalItem(final Value pItem);
}
