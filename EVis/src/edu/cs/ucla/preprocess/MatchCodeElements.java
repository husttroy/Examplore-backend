package edu.cs.ucla.preprocess;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.cs.ucla.model.APICall;
import edu.cs.ucla.model.ControlConstruct;

public class MatchCodeElements extends ASTVisitor {
	HashMap<APICall, ArrayList<Point>> callRanges;
	HashMap<ControlConstruct, ArrayList<Pair<Point, Point>>> controlRanges;
	ArrayList<APICall> calls;
	ArrayList<ControlConstruct> controls;
	APICall focal;
	String methodName;
	ControlConstruct guardBlock;
	ControlConstruct followUpCheck;
	boolean hasVisitedFocal = false;
	
	public MatchCodeElements(String methodName, APICall focal, ArrayList<APICall> apiCalls,
			ArrayList<ControlConstruct> controlConstructs) {
		callRanges = new HashMap<APICall, ArrayList<Point>>();
		controlRanges = new HashMap<ControlConstruct, ArrayList<Pair<Point, Point>>>();
		calls = apiCalls;
		controls = controlConstructs;
		this.focal = focal;
		this.methodName = methodName;
	}

	// create a flag to match the method
	boolean foundMethod = false;

	@Override
	public boolean visit(MethodDeclaration node) {
		String focalAPIName = focal.name.substring(0, focal.name.indexOf('('));
		if (!foundMethod && node.getName().toString().equals(methodName) && node.toString().contains(focalAPIName + "(")) {
			foundMethod = true;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean visit(MethodInvocation node) {
		String apiName = node.getName().toString();

		// receiver expression
		Expression expr = node.getExpression();
		String receiver = null;
		if (expr != null) {
			receiver = expr.toString();
		}

		// arguments
		List<Expression> list = node.arguments();
		ArrayList<String> args = new ArrayList<String>();
		for (Expression arg : list) {
			args.add(arg.toString());
		}

		for (APICall call : calls) {
			// match method call name
			if (!call.name.substring(0, call.name.indexOf('(')).equals(apiName)) {
				continue;
			}

			// match receiver
			if ((receiver == null && call.receiver != null) || (receiver != null && call.receiver == null)) {
				continue;
			}
			if (receiver != null && call.receiver != null) {
				String rcv1 = receiver.replaceAll("[^a-zA-Z0-9]", "");
				String rcv2 = call.receiver.replaceAll("[^a-zA-Z0-9]", "");
				if(!rcv1.contains(rcv2) && !rcv2.contains(rcv1)) {
					continue;
				}
			}

			// match arguments
			if (args.size() != call.arguments.size()) {
				continue;
			}

			boolean flag = true;
			for (int i = 0; i < args.size(); i++) {
				String arg = args.get(i);
				arg = arg.replaceAll("[^a-zA-Z0-9]", "");
				String arg2 = call.arguments.get(i);
				arg2 = arg2.replaceAll("[^a-zA-Z0-9]", "");
				if (!arg.equals(arg2)) {
					flag = false;
					break;
				}
			}

			if (flag) {
				// this is a match!!!
				if(focal.equals(call)) {
					hasVisitedFocal = true;
				}
				int startIndex = node.getStartPosition();
				int endIndex = startIndex + node.getLength();
				Point p = new Point(startIndex, endIndex);
				ArrayList<Point> ranges;
				if (callRanges.containsKey(call)) {
					ranges = callRanges.get(call);
				} else {
					ranges = new ArrayList<Point>();
				}
				ranges.add(p);
				callRanges.put(call, ranges);
			}
		}

		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		Type t = node.getType();
		if (t.isParameterizedType()) {
			ParameterizedType pt = (ParameterizedType) t;
			t = pt.getType();
		}
		String apiName = t.toString();
		if (apiName.contains(".")) {
			apiName = apiName.substring(apiName.lastIndexOf('.') + 1);
		}

		apiName = "new " + apiName;

		// arguments
		List<Expression> list = node.arguments();
		ArrayList<String> args = new ArrayList<String>();
		for (Expression arg : list) {
			args.add(arg.toString());
		}

		for (APICall call : calls) {
			// match method call name
			if (!call.name.substring(0, call.name.indexOf('(')).equals(apiName)) {
				continue;
			}

			// match arguments
			if (args.size() != call.arguments.size()) {
				continue;
			}

			boolean flag = true;
			for (int i = 0; i < args.size(); i++) {
				String arg = args.get(i);
				arg = arg.replaceAll("[^a-zA-Z0-9]", "");
				String arg2 = call.arguments.get(i);
				arg2 = arg2.replaceAll("[^a-zA-Z0-9]", "");
				if (!arg.equals(arg2)) {
					flag = false;
					break;
				}
			}

			if (flag) {
				// this is a match!!!
				if(focal.equals(call)) {
					hasVisitedFocal = true;
				}
				int startIndex = node.getStartPosition();
				int endIndex = startIndex + node.getLength();
				Point p = new Point(startIndex, endIndex);
				ArrayList<Point> ranges;
				if (callRanges.containsKey(call)) {
					ranges = callRanges.get(call);
				} else {
					ranges = new ArrayList<Point>();
				}
				ranges.add(p);
				callRanges.put(call, ranges);
			}
		}

		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		// check for the try block
		for (ControlConstruct cc : controls) {
			if (cc.type.equals("TRY {")) {
				ArrayList<Pair<Point, Point>> ranges;
				if (controlRanges.containsKey(cc)) {
					ranges = controlRanges.get(cc);
				} else {
					ranges = new ArrayList<Pair<Point, Point>>();
				}
				Point keywordRange = new Point(node.getStartPosition(), node
						.getBody().getStartPosition());
				Point blockRange = new Point(node.getStartPosition(),
						node.getStartPosition() + node.getLength());
				ranges.add(Pair.of(keywordRange, blockRange));
				controlRanges.put(cc, ranges);
			}
		}

		// check for the catch clauses
		List<CatchClause> catches = node.catchClauses();
		for (CatchClause c : catches) {
			SingleVariableDeclaration decl = c.getException();
			String exceptionType = decl.getType().toString();
			for (ControlConstruct cc : controls) {
				if (cc.type.equals("CATCH") && cc.guard.equals(exceptionType)) {
					ArrayList<Pair<Point, Point>> ranges;
					if (controlRanges.containsKey(cc)) {
						ranges = controlRanges.get(cc);
					} else {
						ranges = new ArrayList<Pair<Point, Point>>();
					}
					Point keywordRange = new Point(c.getStartPosition(), c
							.getBody().getStartPosition());
					Point blockRange = new Point(c.getStartPosition(),
							c.getStartPosition() + c.getLength());
					ranges.add(Pair.of(keywordRange, blockRange));
					controlRanges.put(cc, ranges);
				}
			}
		}

		// check for the finally block
		Block fblock = node.getFinally();
		if (fblock != null) {
			for (ControlConstruct cc : controls) {
				if (cc.type.equals("FINALLY {")) {
					ArrayList<Pair<Point, Point>> ranges;
					if (controlRanges.containsKey(cc)) {
						ranges = controlRanges.get(cc);
					} else {
						ranges = new ArrayList<Pair<Point, Point>>();
					}
					Point keywordRange = new Point(
							fblock.getStartPosition() - 8,
							fblock.getStartPosition());
					Point blockRange = new Point(fblock.getStartPosition() - 8,
							fblock.getStartPosition() + fblock.getLength());
					ranges.add(Pair.of(keywordRange, blockRange));
					controlRanges.put(cc, ranges);
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		String predicate = node.getExpression().toString();
		String normalized = ProcessUtils
				.getNormalizedPredicate(
						predicate,
						focal.receiver,
						focal.arguments,
						focal.ret);
		String simplified = normalized.replaceAll(" ", "");
		simplified = simplified.replaceAll(",", "");
		
		if(!hasVisitedFocal && guardBlock == null && !focal.normalizedGuard.equals("true")) {
			if(focal.normalizedGuard.replaceAll(",", "").contains(simplified)) {
				guardBlock = new ControlConstruct("IF {", focal.normalizedGuard, focal.originalGuard);
				guardBlock.startIndex1 = node.getStartPosition();
				guardBlock.endIndex1 = node.getExpression().getStartPosition() + node.getExpression().getLength() + 1;
				guardBlock.startIndex2 = node.getStartPosition();
				guardBlock.endIndex2 = node.getStartPosition() + node.getLength();
			}
		}
		
		if(hasVisitedFocal && followUpCheck == null && normalized.contains("ret")) {
			// the predicate is to check about the return value of the focal API
			followUpCheck = new ControlConstruct("IF", normalized, predicate);
			followUpCheck.startIndex1 = node.getStartPosition();
			followUpCheck.endIndex1 = node.getExpression().getStartPosition() + node.getExpression().getLength() + 1;
			followUpCheck.startIndex2 = node.getStartPosition();
			followUpCheck.endIndex2 = node.getStartPosition() + node.getLength();
		}
			
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		if(node.getExpression() == null) {
			return true;
		}
		
		String predicate = node.getExpression().toString();
		String normalized = ProcessUtils
				.getNormalizedPredicate(
						predicate,
						focal.receiver,
						focal.arguments,
						focal.ret);
		String simplified = normalized.replaceAll(" ", "");
		simplified = simplified.replaceAll(",", "");
		
		if(!hasVisitedFocal && guardBlock == null && !focal.normalizedGuard.equals("true")) {
			if(focal.normalizedGuard.replaceAll(",", "").contains(simplified)) {
				guardBlock = new ControlConstruct("LOOP {", focal.normalizedGuard, focal.originalGuard);
				guardBlock.startIndex1 = node.getStartPosition();
				guardBlock.endIndex1 = node.getBody().getStartPosition() - 1;
				guardBlock.startIndex2 = node.getStartPosition();
				guardBlock.endIndex2 = node.getStartPosition() + node.getLength();
			}
		}
		
		if(hasVisitedFocal && followUpCheck == null && normalized.contains("ret")) {
			// the predicate is to check about the return value of the focal API
			followUpCheck = new ControlConstruct("LOOP", normalized, predicate);
			followUpCheck.startIndex1 = node.getStartPosition();
			followUpCheck.endIndex1 = node.getBody().getStartPosition() - 1;
			followUpCheck.startIndex2 = node.getStartPosition();
			followUpCheck.endIndex2 = node.getStartPosition() + node.getLength();
		}
		
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		String predicate = node.getExpression().toString();
		String normalized = ProcessUtils
				.getNormalizedPredicate(
						predicate,
						focal.receiver,
						focal.arguments,
						focal.ret);
		String simplified = normalized.replaceAll(" ", "");
		simplified = simplified.replaceAll(",", "");
		
		if(!hasVisitedFocal && guardBlock == null && !focal.normalizedGuard.equals("true")) {
			if(focal.normalizedGuard.replaceAll(",", "").contains(simplified)) {
				guardBlock = new ControlConstruct("LOOP {", focal.normalizedGuard, focal.originalGuard);
				guardBlock.startIndex1 = node.getStartPosition();
				guardBlock.endIndex1 = node.getExpression().getStartPosition() + node.getExpression().getLength() + 1;
				guardBlock.startIndex2 = node.getStartPosition();
				guardBlock.endIndex2 = node.getStartPosition() + node.getLength();
			}
		}
		
		if(hasVisitedFocal && followUpCheck == null && normalized.contains("ret")) {
			// the predicate is to check about the return value of the focal API
			followUpCheck = new ControlConstruct("LOOP {", normalized, predicate);
			followUpCheck.startIndex1 = node.getStartPosition();
			followUpCheck.endIndex1 = node.getExpression().getStartPosition() + node.getExpression().getLength() + 1;
			followUpCheck.startIndex2 = node.getStartPosition();
			followUpCheck.endIndex2 = node.getStartPosition() + node.getLength();
		}
		
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		String predicate = node.getExpression().toString();
		String normalized = ProcessUtils
				.getNormalizedPredicate(
						predicate,
						focal.receiver,
						focal.arguments,
						focal.ret);
		String simplified = normalized.replaceAll(" ", "");
		simplified = simplified.replaceAll(",", "");
		
		if(!hasVisitedFocal && guardBlock == null && !focal.normalizedGuard.equals("true")) {
			if(focal.normalizedGuard.replaceAll(",", "").contains(simplified)) {
				guardBlock = new ControlConstruct("LOOP {", focal.normalizedGuard, focal.originalGuard);
				guardBlock.startIndex1 = node.getStartPosition();
				guardBlock.endIndex1 = node.getExpression().getStartPosition() + node.getExpression().getLength() + 1;
				guardBlock.startIndex2 = node.getStartPosition();
				guardBlock.endIndex2 = node.getStartPosition() + node.getLength();
			}
		}
		
		if(hasVisitedFocal && followUpCheck == null && normalized.contains("ret")) {
			// the predicate is to check about the return value of the focal API
			String normalize = ProcessUtils
					.getNormalizedPredicate(
							predicate,
							focal.receiver,
							focal.arguments,
							focal.ret);
			followUpCheck = new ControlConstruct("LOOP {", normalize, predicate);
			followUpCheck.startIndex1 = node.getStartPosition();
			followUpCheck.endIndex1 = node.getExpression().getStartPosition() + node.getExpression().getLength() + 1;
			followUpCheck.startIndex2 = node.getStartPosition();
			followUpCheck.endIndex2 = node.getStartPosition() + node.getLength();
		}
		
		return true;
	}
}
