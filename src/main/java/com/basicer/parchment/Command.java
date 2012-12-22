package com.basicer.parchment;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import com.basicer.parchment.parameters.Parameter;

public abstract class Command {
	
	public static Parameter[] parseLine(Context ctx, StringReader s) {
		List<Parameter> out = new ArrayList<Parameter>();
		StringBuilder current = new StringBuilder();
		char in = '\0';
		int brackets = 0;
		int procs = 0;
		int r;
		try {
			while ( (r = s.read()) > 0) {
				char c = (char)r;
				boolean append = false;
				if ( brackets > 0 ) {
					
					if ( c == '}' ) --brackets;
					if ( brackets != 0 || procs != 0 || in == '"' ) {
						append = true;
					} 
				} else if ( in == '"' ) {
					if ( c == '"' && procs == 0) in = '\0';
					if ( c == '{' ) { ++brackets; append = true; }
					else {
						if ( c == ']' ) --procs;
						else if ( c == '[' ) ++procs;
						append = true;
					}
				} else if ( in == '[' ) {
					if ( c == '{' ) { ++brackets; }
					else if ( c == ']' ) { 
						if ( --procs == 0 ) {
							out.add(Parameter.from("PROC:" + current.toString()));
							current.setLength(0);
						}
						else append = true;
					} else if ( c == ' ' ) {
						if ( procs == 0 ) in = '\0';
						else append = true;
					} else if ( c == '[' ) {
						if ( ++procs > 1) {
							append = true;
						} 						
					} else append = true;
				} else {
					if ( c == '"' ) in = c;
					else if ( c == '{' ) { 
						++brackets; 
					} else if ( c == '[' ) { 
						++procs; in = c; 
					}else if ( c == ' ' ) {
						out.add(Parameter.from(current.toString()));
						current.setLength(0);
					} else if ( c == '\n' || c == ';' ) {
						break;
					} else {
						append = true;
					}
				}
				if ( append ) {
					if ( c == '\\' && brackets == 0 && procs == 0 ) current.append(translateSlashCode(s.read()));
					else current.append(c);
				}
			}
			if ( current.length() > 0 ) out.add(Parameter.from(current.toString()));
		} catch (IOException e) {
			throw new Error(e);
		}
		
		return out.toArray(new Parameter[0]);
	}
	
	private static char translateSlashCode(int i) {
		if ( i < 1 ) return '\\';
		switch ( (char) i) {
			case 'n': return '\n';
			case 't': return 'X';
			default: return (char) i;
		}
	}
	
	public abstract Parameter execute(Context ctx, Parameter[] args);
	
}
