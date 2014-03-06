package br4cp;

/**
 * List all protocols for the experimentation
 * 
 * @author leberre
 *
 */
public enum Protocol {
	/** Basic Tests */
	BT,
	/** Full Configuration Protocol */
	FCP,
	/** Greedy, unpriced, configuration */
	GC_U,
	/** Greedy configuration at minimal price */
	GC_p,
	/** Approximating Global Inverse Consistency */
	GIC_Approx,
	/** Full configuration, alternative values */
	FC_Alt,
	/** Greedy configuration, alternative values */
	GC_Alt,
	/** Conflict Generation */
	CG,
	/** Configuration with explanations */
	GC_Expl,
	/** Configuration with restorations */
	GC_Res
}
