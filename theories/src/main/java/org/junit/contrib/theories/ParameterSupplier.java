package org.junit.contrib.theories;

import java.util.List;

public abstract class ParameterSupplier {
	public abstract List<PotentialAssignment> getValueSources(ParameterSignature sig);
}
