package org.junit.contrib.tests.theories.extendingwithstubs;

import java.util.List;

import org.junit.AssumptionViolatedException;
import org.junit.contrib.theories.PotentialAssignment;

public abstract class ReguessableValue extends PotentialAssignment {
    public abstract List<ReguessableValue> reguesses(AssumptionViolatedException e);
}
