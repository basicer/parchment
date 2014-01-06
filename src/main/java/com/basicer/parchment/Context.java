package com.basicer.parchment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.basicer.parchment.parameters.DelegateParameter;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

public class Context {
	
	private Context parent;
	
	private SpellFactory spellfactory;
	private Map<String, ParameterPtr> variables;
	private Map<String, TCLCommand> procs;
	
	public Context() { 
		variables = new HashMap<String, ParameterPtr>();
		procs = new HashMap<String, TCLCommand>();
		
	}
	
	private Context(Context parent) {
		variables = new HashMap<String, ParameterPtr>();
		procs = new HashMap<String, TCLCommand>();
		this.parent = parent;
	}
	
	public void push() {
		Context new_parent = new Context(parent);
		Map<String, ParameterPtr> swap = new_parent.variables;
		new_parent.variables = variables;
		variables = swap;
	}
	
	public Parameter get(String var) {
		ParameterPtr ptr = variables.get(var);
		if ( ptr == null ) return null;
		return ptr.getValue();
	}
	
	public Parameter getRespectingGlobals(String var) {
		
		if ( var.equals("errorInfo") ) return top().get("errorInfo");
		if ( var.equals("::errorInfo") ) return top().get("errorInfo");
		
		if ( var.equals("target") ) return getTarget();
		if ( var.equals("caster") ) return getCaster();
		if ( var.equals("world") ) return Parameter.from(getWorld());
		if ( var.equals("source") ) return Parameter.from(getSource());
		if ( var.equals("this") ) return getThis();
		
		ParameterPtr ptr = variables.get(var);
		if ( ptr == null ) return null;
		return ptr.getValue();
	}
	
	public boolean hasRespectingGlobals(String var) {
		if ( has(var) ) return true;
		if ( getRespectingGlobals(var) != null ) return true;
		return false;
	}
	
	public void put(String var, Parameter value) {
		ParameterPtr nv = getRaw(var);
		if ( nv != null ) {
			nv.setValue(value);
		} else {
			nv = new ParameterPtr(value);
			variables.put(var, nv);
		}
	}
	

	
	public void pop() {
		if ( parent == null ) throw new IllegalStateException("Can't pop base context.");
		this.variables = parent.variables;
	}
	
		
	public SpellFactory getSpellFactory() {
		if ( spellfactory != null ) return spellfactory;
		if ( parent != null ) return parent.getSpellFactory();
		return null;
		
	}
	
	public TCLCommand getCommand(String name) {
		if ( procs.containsKey(name)) return procs.get(name);
		if ( spellfactory != null ) {
			TCLCommand r = spellfactory.get(name);
			if ( r != null ) return r;
		}
		if ( parent != null ) return parent.getCommand(name);
		return null;
		
	}
	
	public void setSpellFactory(SpellFactory val) {
		spellfactory = val;
	}
	
	public Parameter getThis() {
		return resolve("this");
	}
	
	public void setThis(Parameter parameter) {
		put("this", parameter);
	}

	
	public Parameter getCaster() {
		return resolve("caster");
	}

	public void setCaster(Parameter caster) {
		put("caster", caster);
	}

	public Parameter getTarget() {
		return resolve("target");
	}

	public void setTarget(Parameter target) {
		put("target", target);
	}
	
	public String getSource() {
		Parameter p = resolve("source");
		if ( p == null ) return null;
		return p.asString();
	}

	public void setSource(String source) {
		put("source", Parameter.from(source));
	}
	
	public World getWorld() {
		Parameter w = resolve("world");
		if ( w == null ) return null;
		return w.as(World.class);
	}

	public void setWorld(Parameter target) {
		put("world", target);
	}

	public ArrayList<Parameter> getArgs() {
		Parameter lg = get("args");
		if ( lg == null ) return new ArrayList<Parameter>();
		if ( lg instanceof ListParameter ) {
			ListParameter ll = (ListParameter)lg;
			return ll.asArrayList();
		}
		return new ArrayList<Parameter>();
	}
	
	public void sendDebugMessage(String msg) {
		Parameter rp = getCaster();
		if ( rp != null ) {
			Player p = rp.as(Player.class);
			if ( p == null ) Bukkit.getConsoleSender().sendRawMessage(msg);
			else p.sendRawMessage(msg);
		} else {
			Debug.info("%s", msg);
		}
	}
	
	public Context createSubContext() {
		Context ctx = new Context();
		ctx.parent = this;
		return ctx;
	}
	
	/*
	public Context createBoundSubContext(Context bound) {
		Context ctx = new Context();
		ctx.variables = new HashMap<String, ParameterPtr>(bound.variables);
		ctx.parent = parent;
		ctx.procs = bound.procs;
		return ctx;
	}
	 */
	
	public Context copyAndMergeProcs(Context whereProcsAre) {
		Context ctx = new Context();
		ctx.variables = variables;
		ctx.parent = parent;
		ctx.procs = new HashMap<String, TCLCommand>(procs);
		ctx.spellfactory = spellfactory;
		ctx.importProcs(whereProcsAre);
		return ctx;
		
	}
	
	public void importProcs(Context whereProcsAre) {
		for ( String s : whereProcsAre.procs.keySet() ) {
			procs.put(s, whereProcsAre.getCommand(s));
		}
		
	}

	public Context up(int level) {
		Context out = this;
		for ( int i = 0; i < level; ++i ) {
			out = out.parent;
		}
		return out;
	}

	public void upvar(int level, String var) {
		upvar(level, var, var);

	}

	public boolean upvar(int level, String var, String as) {
		Context ctx = up(level);
		if ( ctx == null ) return false;
		ParameterPtr p = ctx.getRaw(var);
		if ( p == null ) {
			p = new ParameterPtr();
			ctx.setRaw(var, p);
		}
		setRaw(as, p);
		return true;
	}
	
	public Parameter resolve(String var) {
		Parameter val = get(var);
		if ( val == null ) {
			if ( parent != null ) return parent.resolve(var);
			return null;
		}
		
		return val;
	}
	
	public <T extends Parameter> T getWithTypeOr(String var, T def) {
		Parameter p = get(var);
		if ( p == null ) return def;
		T result = (T)p.cast(def.getClass());
		if ( result == null ) return def;
		return result;
	}
	
	private ParameterPtr getRaw(String var) {
		return variables.get(var);
	}
	
	private void setRaw(String var, ParameterPtr p) {
		variables.put(var, p);
	}
	
	static class ParameterPtr {
		public ParameterPtr(Parameter val) {
			this.val = val;
		}

		public ParameterPtr() {

		}
		private Parameter val;

		Parameter getValue() {
			return val;
		}

		void setValue(Parameter val) {
			this.val = val;
		}
	}

	public void setCommand(String name, TCLCommand proc) {
		procs.put(name, proc);
		
	}
	
	public boolean hasArgs() {
		return this.getArgs().size() > 0;
	}

	public Context top() {
		Context ctx = this;
		while ( ctx.parent != null ) ctx = ctx.parent;
		return ctx;
	}

	public boolean has(String name) {
		ParameterPtr p = this.getRaw(name);
		if ( p == null ) return false;
		return true;
		//return p.val != null;
	}

	public void linkVariableFromContext(Context other, String var) {
		ParameterPtr ptr = other.getRaw(var);
		if ( ptr == null ) {
			ptr = new ParameterPtr();
			other.setRaw(var, ptr);
		}
		Debug.trace("Setting " + var + " to " + ptr);
		this.setRaw(var, ptr);
	}

	public String getDebuggingString() {
		int i = 0;
		Context x = this;
		while ( x != null ) {
			++i;
			x = x.up(1);
		}
		String out = "Depth: " + i + "\n Variables [\n";
		for ( String s : this.variables.keySet() ) {
			Parameter p = variables.get(s).val;
			out += s + " = " + (p == null ? "null" : p.toString()) + "\n";
		}
		out   += "]\nProcs [\n";
		
		for ( String s : this.procs.keySet() ) {
			out += s + "\n";
		}
		out += "]\n SpellFacotry = " + spellfactory + " / " + getSpellFactory() + "\n";
		if ( parent != null ) {
			out += "\n==== Parent =====\n";
			out += parent.getDebuggingString();
		}
		
		return out;
	}

	public void putProc(String string, TCLCommand tclCommand) {
		this.procs.put(string, tclCommand);
		
	}

	public void unset(String name) {
		this.variables.remove(name);
	}

	public Context mergeAndCopyAsGlobal() {
		Context b = new Context();
		writeAllStuffInto(b);
		return b;
	}

	protected void writeAllStuffInto(Context other) {
		if ( parent != null ) parent.writeAllStuffInto(other);

		if ( spellfactory != null ) other.spellfactory = spellfactory;
		
		for ( String k : this.variables.keySet() ) {
			other.put(k,  variables.get(k).val);
		}

		for ( String k : this.procs.keySet() ) {
			other.procs.put(k,  procs.get(k));
		}

		
	}







	
}
