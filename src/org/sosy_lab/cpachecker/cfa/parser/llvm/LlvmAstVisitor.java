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
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.ADeclaration;
import org.sosy_lab.cpachecker.cfa.ast.FileLocation;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.c.CLabelNode;
import org.sosy_lab.cpachecker.cfa.model.c.CAssumeEdge;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.BlankEdge;
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

  public LlvmAstVisitor() {
    basicBlockId = 0;

    functions = new TreeMap<>();
    cfaNodes = TreeMultimap.create();
    globalDeclarations = new ArrayList<>();
  }

  public enum Behavior { CONTINUE, STOP }

  public void visit(final Module pItem) {
    /* create globals */
    iterateOverGlobals(pItem);

    /* create CFA for all functions */
    iterateOverFunctions(pItem);
  }

  private void iterateOverGlobals(final Module pItem) {
    Value globalItem = pItem.getFirstGlobal();
    /* no globals? */
    if (globalItem == null)
      return;

    Value globalItemLast = pItem.getLastGlobal();
    assert globalItemLast != null;

    while (true) {
      Behavior behavior = visitGlobalItem(globalItem);
      if (behavior == Behavior.CONTINUE) {
        /* we processed the last global variable? */
        if (globalItem.equals(globalItemLast))
          break;

        globalItem = globalItem.getNextGlobal();

      } else {
        assert behavior == Behavior.STOP : "Unhandled behavior type " + behavior;
        return;
      }
    }
  }

  private void addNode(String funcName, CFANode nd) {
      cfaNodes.put(funcName, nd);
  }

  private void addEdge(CFAEdge edge) {
      edge.getPredecessor().addLeavingEdge(edge);
      edge.getSuccessor().addEnteringEdge(edge);
  }

  private void iterateOverFunctions(final Module pItem) {
    Value func = pItem.getFirstFunction();
    if (func == null)
      return;

    Value funcLast = pItem.getFirstFunction();
    assert funcLast != null;

    while (true) {
      // skip declarations
      if (func.isDeclaration()) {
        if (func.equals(funcLast))
          break;

        func = func.getNextFunction();
        continue;
      }

      String funcName = func.getValueName();
      assert !funcName.isEmpty();

      // handle the function definition
      FunctionEntryNode en = visitFunction(func);
      addNode(funcName, en);

      // create the basic blocks and instructions of the function.
      // A basic block is mapped to a pair <entry node, exit node>
      SortedMap<Long, BasicBlockInfo> basicBlocks = new TreeMap<>();
      CLabelNode entryBB = iterateOverBasicBlocks(func, funcName, basicBlocks);

      // add the edge from the entry of the function to the first
      // basic block
      //BlankEdge.buildNoopEdge(en, entryBB);
      addEdge(new BlankEdge("entry", FileLocation.DUMMY,
                             en, entryBB, "Function start edge"));

      // add branching between instructions
      List<CFANode> termnodes = addJumpsBetweenBasicBlocks(func, basicBlocks);

      // lead every termination node to the one unified termination node of the CFA
      CFANode exitNode = en.getExitNode();
      for (CFANode n : termnodes) {
        System.out.println("Got termnode in func " + funcName);
        addEdge(new BlankEdge("to_exit", FileLocation.DUMMY,
                              n, exitNode, "Unified exit edge"));
      }

      functions.put(funcName, en);

      // process the next function
      if (func.equals(funcLast))
        break;

      func = func.getNextFunction();
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
                                            String funcName,
                                            SortedMap<Long, BasicBlockInfo> basicBlocks) {
    assert pItem.isFunction();

    BasicBlock BB = pItem.getFirstBasicBlock();
    if (BB == null)
      return null;

    BasicBlock lastBB = pItem.getLastBasicBlock();
    assert lastBB != null;

    CLabelNode entryBB = null;
    while (true) {
      // process this basic block
      CLabelNode label = new CLabelNode(funcName, getBBName(BB));
      addNode(funcName, label);
      if (entryBB == null)
        entryBB = label;

      BasicBlockInfo bbi = handleInstructions(funcName, BB);
      basicBlocks.put(BB.getAddress(), new BasicBlockInfo(label, bbi.getExitNode()));

      // add an edge from label to the first node
      // of this basic block
      addEdge(new BlankEdge("label_to_first", FileLocation.DUMMY,
                            label, bbi.getEntryNode(), "edge to first instr"));

      // did we process all basic blocks?
      if (BB.equals(lastBB))
        break;

      BB = BB.getNextBasicBlock();
    }

    assert entryBB != null || basicBlocks.isEmpty();
    return entryBB;
  }

  /**
   * Add branching edges between nodes, returns the set of termination nodes
   */
  private List<CFANode> addJumpsBetweenBasicBlocks(final Value pItem,
                                                   SortedMap<Long, BasicBlockInfo> basicBlocks) {
    assert pItem.isFunction();

    List<CFANode> termnodes = new ArrayList<CFANode>();

    BasicBlock BB = pItem.getFirstBasicBlock();
    if (BB == null)
      return termnodes;

    BasicBlock lastBB = pItem.getLastBasicBlock();
    assert lastBB != null;

    // for every basic block, get the last instruction and
    // add edges from it to labels where it jumps
    while (true) {
      Value terminatorInst = BB.getLastInstruction();
      if (terminatorInst == null) {
        if (BB.equals(lastBB))
          break;

        // FIXME: we should really create iterators...
        BB = BB.getNextBasicBlock();
        continue;
      }

      assert terminatorInst.isTerminatorInst();
      CFANode brNode = basicBlocks.get(BB.getAddress()).getExitNode();

      int succNum = terminatorInst.getNumSuccessors();
      if (succNum == 0) {
        termnodes.add(brNode);
        if (BB.equals(lastBB))
          break;

        BB = BB.getNextBasicBlock();
        continue;
      }

      // get the operands and add branching edges
      for (int i = 0; i < succNum; ++i) {
        BasicBlock succ = terminatorInst.getSuccessor(i);
        CLabelNode label = (CLabelNode)basicBlocks.get(succ.getAddress()).getEntryNode();

        // FIXME
        /*
        CExpression expr = null;

        addEdge(new CAssumeEdge("T", FileLocation.DUMMY,
                        brNode, (CFANode)label, expr, true));
        */

        // FIXME
        addEdge(new BlankEdge("?", FileLocation.DUMMY,
                               brNode, (CFANode)label, Integer.toString(i)));
      }

      // did we processed all basic blocks?
      if (BB.equals(lastBB))
        break;

      BB = BB.getNextBasicBlock();
    }

    return termnodes;
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

  /**
   * Create a chain of nodes and edges corresponding to one basic block.
   */
  private BasicBlockInfo handleInstructions(String funcName, final BasicBlock pItem) {
    Value I = pItem.getFirstInstruction();
    if (I == null)
      return null;

    Value lastI = pItem.getLastInstruction();
    assert lastI != null;

    CFANode prevNode = new CFANode(funcName);
    CFANode firstNode = prevNode;
    CFANode curNode = new CFANode(funcName);
    addNode(funcName, prevNode);
    addNode(funcName, curNode);

    while (true) {
      // process this basic block
      CStatement expr = visitInstruction(I);

      // build an edge with this expression over it
      // TODO -- FIXME
      addEdge(BlankEdge.buildNoopEdge(prevNode, curNode));

      // did we processed all instructions in this basic block?
      if (I.equals(lastI))
        break;

      I = I.getNextInstruction();

      prevNode = curNode;
      curNode = new CFANode(funcName);
      addNode(funcName, curNode);
    }

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
  protected abstract CStatement visitInstruction(final Value pItem);
  protected abstract Behavior visitGlobalItem(final Value pItem);
}
