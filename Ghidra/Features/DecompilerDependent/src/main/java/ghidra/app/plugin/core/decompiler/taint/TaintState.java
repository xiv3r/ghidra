/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.plugin.core.decompiler.taint;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

import com.contrastsecurity.sarif.SarifSchema210;

import ghidra.app.decompiler.*;
import ghidra.app.script.GhidraScript;
import ghidra.app.services.ConsoleService;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Program;
import ghidra.program.model.pcode.*;
import ghidra.program.model.symbol.Symbol;
import ghidra.program.model.symbol.SymbolTable;
import ghidra.util.Msg;
import ghidra.util.classfinder.ClassSearcher;
import ghidra.util.classfinder.ExtensionPoint;

/**
 * The interface for the methods that collect desired taint information from the decompiler window and store them
 * for construction of queries and indexing.
 * <p>
 * NOTE:  ALL TaintState CLASSES MUST END IN "TaintState".  If not,
 * the ClassSearcher will not find them.
 */
public interface TaintState extends ExtensionPoint {

	public enum MarkType {
		SOURCE, SINK, GATE
	}

	public enum QueryType {
		SRCSINK, DEFAULT, CUSTOM
	}

	public enum TaskType {
		SET_TAINT, SET_DELTA, APPLY_DELTA
	}

	public static TaintState newInstance(TaintPlugin plugin, String type) {
		List<Class<? extends TaintState>> list = ClassSearcher.getClasses(TaintState.class)
				.stream()
				.toList();
		Class<?>[] constructorArgumentTypes = {TaintPlugin.class};
		Object[] args = new Object[1];
		args[0] = plugin;
		for (Class<? extends TaintState> clazz : list) {
			if (clazz.getName().toLowerCase().contains(type)) {
				try {
					Constructor<?> constructor = clazz.getConstructor(constructorArgumentTypes);
					Object obj = constructor.newInstance(plugin);
					return TaintState.class.cast(obj);
				}
				catch (Exception e) {
					throw new RuntimeException("Unable to instantiate TaintState");
				}
			}
		}
		Msg.error(plugin, "No match for engine = "+type);
		return null;
	}

	public String getName();

	/**
	 * Perform a Source-Sink query on the index database.
	 * 
	 * @param program the program whose pcode is being queried.
	 * @param tool - 
	 * @param queryType true -> perform the default query (i.e., do not build the query from the selected source) 
	 * @return success
	 */
	public boolean queryIndex(Program program, PluginTool tool, QueryType queryType);

	public TaintLabel toggleMark(MarkType mtype, ClangToken token) throws PcodeException;

	public Set<TaintLabel> getTaintLabels(MarkType mtype);

	public boolean isValid();

	public AddressSet getTaintAddressSet();

	public void setTaintAddressSet(AddressSet aset);

	public void augmentAddressSet(ClangToken token);

	public void clearTaint();

	public boolean isSink(HighVariable hvar);

	public void clearMarkers();

	public void loadTaintData(Program program, File sarif_file);

	public SarifSchema210 getData();

	public void clearData();

	public TaintOptions getOptions();

	// predicate that indicates there are sources, sinks, or gates.
	public boolean hasMarks();

	public boolean wasCancelled();

	public void setCancellation(boolean status);

	public void setTaintVarnodeMap(Map<Address, Set<TaintQueryResult>> vmap, TaskType delta);

	public Map<Address, Set<TaintQueryResult>> getTaintVarnodeMap();

	public Set<TaintQueryResult> getQuerySet(Address addr);

	public void buildIndex(List<String> param_list, String engine_path, String facts_path,
			String index_directory);

	public GhidraScript getExportScript(ConsoleService console, boolean perFunction);

	public void setTaskType(TaskType taskType);

	public TaintLabel getLabelForToken(MarkType type, ClangToken token);

	public static String varName(ClangToken token, boolean append) {
		String tokenText = token.getText();
		if (token instanceof ClangFieldToken ftoken) {
			ClangVariableToken vtoken = TaintState.getParentToken(ftoken);
			if (vtoken == null) {
				return tokenText;
			}
			HighVariable hv = vtoken.getHighVariable();
			Varnode rep = hv.getRepresentative();
			return rep.getAddress().toString();
		}
		
		HighVariable hv = token.getHighVariable();
		if (hv != null) {
			if (hv instanceof HighLocal && token.getVarnode() == null) {
				int offset = hv.getOffset();
				Varnode rep = hv.getRepresentative();
				return rep.getAddress().subtract(offset).toString();
			}
			return hvarName(hv);
		}
		return tokenText;
	}

	private static String hvarName(HighVariable hv) {
		Varnode rep = hv.getRepresentative();
		if (rep.getAddress().isUniqueAddress()) {
			HighFunction hf = hv.getHighFunction();
			DynamicHash dynamicHash = new DynamicHash(rep, hf);
			return "hv"+Long.toString(dynamicHash.getHash());
		}
		if (hv.getName() == null || hv.getName().equals("UNNAMED")) {
			if (hv instanceof HighConstant || hv instanceof HighOther) {
				Address addr = rep.getAddress();
				return addr.toString();
			}
			if (hv instanceof HighLocal) {
				Address addr = rep.getAddress();
				return addr.toString();
			}
			if (hv instanceof HighGlobal) {
				Function fn = hv.getHighFunction().getFunction();
				SymbolTable symbolTable = fn.getProgram().getSymbolTable();
				Address addr = rep.getAddress();
				Symbol symbol = symbolTable.getPrimarySymbol(addr);
				if (symbol != null) {
					return symbol.getName();
				}
				return addr.toString();
			}
			return null;
		}
		return hv.getName();
	}

	public static ClangVariableToken getParentToken(ClangFieldToken token) {
		ClangTokenGroup group = (ClangTokenGroup) token.Parent();
		Iterator<ClangToken> iterator = group.tokenIterator(true);
		ClangVariableToken parent = null;
		while (iterator.hasNext()) {
			ClangToken next = iterator.next();
			if (next instanceof ClangVariableToken vtoken) {
				parent = vtoken;
			}
			if (next instanceof ClangFieldToken ftoken) {
				if (ftoken.equals(token)) {
					return parent;
				}
			}
		}
		return null;
	}

	public static boolean isActualParam(ClangToken token) {
		PcodeOp pcodeOp = token.getPcodeOp();
		if (pcodeOp != null) {
			String mnemonic = pcodeOp.getMnemonic();
			if (mnemonic.contains("CALL")) {
				for (Varnode input : pcodeOp.getInputs()) {
					if (input.equals(token.getVarnode())) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
