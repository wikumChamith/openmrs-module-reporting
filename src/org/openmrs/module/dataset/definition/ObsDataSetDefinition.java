/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dataset.definition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.module.cohort.definition.CohortDefinition;
import org.openmrs.module.dataset.column.DataSetColumn;
import org.openmrs.module.dataset.column.SimpleDataSetColumn;
import org.openmrs.module.dataset.definition.evaluator.ObsDataSetEvaluator;

/**
 * Definition of a dataset that produces one-row-per-obs. Output might look like: patientId,
 * question, questionConceptId, answer, answerConceptId, obsDatetime, encounterId 123,
 * "WEIGHT (KG)", 5089, 70, null, "2007-05-23", 2345 123, "OCCUPATION", 987, "STUDENT", 988,
 * "2008-01-30", 2658
 * 
 * @see ObsDataSetEvaluator
 */
public class ObsDataSetDefinition extends BaseDataSetDefinition {
	
	private static final long serialVersionUID = 1L;
	
    // ***** FIXED COLUMNS *****
	public static DataSetColumn PATIENT_ID = new SimpleDataSetColumn("patientId", Integer.class);
	public static DataSetColumn QUESTION = new SimpleDataSetColumn("question", String.class);
	public static DataSetColumn QUESTION_CONCEPT_ID = new SimpleDataSetColumn("questionConceptId", Integer.class);
	public static DataSetColumn ANSWER = new SimpleDataSetColumn("answer", Object.class);
	public static DataSetColumn ANSWER_CONCEPT_ID = new SimpleDataSetColumn("answerConceptId", Integer.class);
	public static DataSetColumn OBS_DATETIME = new SimpleDataSetColumn("obsDatetime", Date.class);
	public static DataSetColumn ENCOUNTER_ID = new SimpleDataSetColumn("encounterId", Integer.class);
	public static DataSetColumn OBSGROUP_ID = new SimpleDataSetColumn("obsGroupId", Integer.class);
	
	// ****** PROPERTIES *******
	
	private Collection<Concept> questions;
	private CohortDefinition filter;
	private Date fromDate;
	private Date toDate;

	/**
	 * Default constructor
	 */
	public ObsDataSetDefinition() {
		super();
	}
	
	/**
	 * Public constructor
	 * 
	 * @param name
	 * @param description
	 * @param questions
	 */
	public ObsDataSetDefinition(String name, String description, Set<Concept> questions) {
		super(name, description);
		setQuestions(questions);
	}
	
	//****** INSTANCE METHODS ******
	
	/** 
	 * @see DataSetDefinition#getColumns()
	 */
	public List<DataSetColumn> getColumns() {
		return Arrays.asList(PATIENT_ID, QUESTION, QUESTION_CONCEPT_ID, ANSWER, 
							 ANSWER_CONCEPT_ID, OBS_DATETIME, ENCOUNTER_ID, OBSGROUP_ID);
	}
	
	//****** PROPERTY ACCESS ********

	/**
	 * @return the questions
	 */
	public Collection<Concept> getQuestions() {
		if (questions == null) {
			questions = new HashSet<Concept>();
		}
		return questions;
	}

	/**
	 * @param questions the questions to set
	 */
	public void setQuestions(Collection<Concept> questions) {
		this.questions = questions;
	}

	/**
	 * @return the filter
	 */
	public CohortDefinition getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(CohortDefinition filter) {
		this.filter = filter;
	}

	/**
	 * @return the fromDate
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * @param fromDate the fromDate to set
	 */
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * @return the toDate
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * @param toDate the toDate to set
	 */
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
}
