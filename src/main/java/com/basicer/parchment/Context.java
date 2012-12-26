package com.basicer.parchment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.World;

import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

public class Context {
	
	private Context parent;
	
	private Map<String, ParameterPtr> variables;
	
	public Context() { 
		variables = new HashMap<String, ParameterPtr>();
		
	}
	
	private Context(Context parent) {
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
		return ptr.val;
	}
	
	public Parameter getRespectingGlobals(String var) {
		if ( var.equals("target") ) return getTarget();
		if ( var.equals("caster") ) return getCaster();
		if ( var.equals("world") ) return Parameter.from(getWorld());
		if ( var.equals("server") ) return Parameter.from(getServer());
		if ( var.equals("source") ) return Parameter.from(getSource());
		
		ParameterPtr ptr = variables.get(var);
		if ( ptr == null ) return null;
		return ptr.val;
	}
	
	public void put(String var, Parameter value) {
		ParameterPtr nv = getRaw(var);
		if ( nv != null ) {
			nv.val = value;
		} else {
			nv = new ParameterPtr();
			nv.val = value;
			variables.put(var, nv);
		}
	}
	

	
	public void pop() {
		if ( parent == null ) throw new IllegalStateException("Can't pop base context.");
		this.variables = parent.variables;
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
		return w.asWorld();
	}

	public void setWorld(Parameter target) {
		put("world", target);
	}
	
	public Server getServer() {
		Parameter s = resolve("server");
		if ( s == null ) return null;
		return s.asServer();
	}

	public void setServer(Parameter target) {
		put("server", target);
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
		Parameter p = getCaster();
		if ( p != null ) {
			p.asPlayer().sendMessage(msg);
		} else {
			System.out.println(msg);
		}
	}
	
	public Context createSubContext() {
		Context ctx = new Context();
		ctx.parent = this;
		return ctx;
	}
	
	public Context createBoundSubContext(Context bound) {
		Context ctx = new Context();
		ctx.variables = bound.variables;
		ctx.parent = this;
		return ctx;
	}
	
	
	public Context up(int level) {
		Context out = this;
		for ( int i = 0; i < level; ++i ) {
			out = out.parent;
		}
		return out;
	}
	
	public void upvar(int level, String var) {
		Context ctx = up(level);
		ParameterPtr p = ctx.getRaw(var);
		if ( p == null ) {
			p = new ParameterPtr();
			ctx.setRaw(var, p);
		}
		setRaw(var, p);
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
		public Parameter val;
	}


	
}
