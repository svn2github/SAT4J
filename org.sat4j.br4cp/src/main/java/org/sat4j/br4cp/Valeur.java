package org.sat4j.br4cp;

public class Valeur {
	public final String variable;
	public int valeur;

	public Valeur(String var, int val) {
		this.variable = var;
		this.valeur = val;
	}

	public String toString() {
		return this.variable + "=" + this.valeur;
	}
}
