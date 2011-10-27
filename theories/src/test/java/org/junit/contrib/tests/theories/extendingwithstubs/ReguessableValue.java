package org.junit.contrib.tests.theories.extendingwithstubs;

import org.junit.contrib.theories.PotentialAssignment;
import org.junit.internal.AssumptionViolatedException;

import java.util.List;

public abstract class ReguessableValue extends PotentialAssignment {
    public abstract List<ReguessableValue> reguesses(AssumptionViolatedException e);
}
