/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.util;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLFacet;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;

import javax.annotation.Nonnull;

/**
 * @author Lorenz Buehmann
 *
 */
public class ClassExpressionLiteralCombination implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>>, OWLDataRangeVisitorEx<Set<OWLDataRange>>{
	
	private OWLDataFactory df =  new OWLDataFactoryImpl();

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLClass ce) {
		Set<OWLClassExpression> expressions = new HashSet<>(1);
		expressions.add(ce);
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> expressions = new HashSet<>();
		Set<Set<OWLClassExpression>> combinations = new HashSet<>();
		for (int i = 0; i < ce.getOperands().size(); i++) {
			Set<OWLClassExpression> tmp = new HashSet<>();
			combinations.add(tmp);
		}
		for (OWLClassExpression operand : ce.getOperands()) {
			Set<Set<OWLClassExpression>> combinationsTmp = new HashSet<>();
			Set<OWLClassExpression> newOperands = operand.accept(this);
			for (Set<OWLClassExpression> set : combinations) {
				for (OWLClassExpression newOp : newOperands) {
					Set<OWLClassExpression> tmp = new HashSet<>();
					tmp.addAll(set);
					tmp.add(newOp);
					combinationsTmp.add(tmp);
				}
			}
			combinations = combinationsTmp;
		}
		for (Set<OWLClassExpression> combination : combinations) {
			expressions.add(df.getOWLObjectIntersectionOf(combination));
		}
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectUnionOf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectComplementOf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectSomeValuesFrom ce) {
		Set<OWLClassExpression> expressions = new HashSet<>();
		Set<OWLClassExpression> newFillers = ce.getFiller().accept(this);
		for (OWLClassExpression newFiller : newFillers) {
			expressions.add(df.getOWLObjectSomeValuesFrom(ce.getProperty(), newFiller));
		}
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectAllValuesFrom ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectHasValue)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectHasValue ce) {
		Set<OWLClassExpression> expressions = new HashSet<>();
		expressions.add(ce);
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectMinCardinality)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectMinCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectExactCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectMaxCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectHasSelf)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectHasSelf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectOneOf)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLObjectOneOf ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataSomeValuesFrom ce) {
		Set<OWLClassExpression> expressions = new HashSet<>();
		Set<OWLDataRange> newDataRanges = ce.getFiller().accept(this);
		for (OWLDataRange newDataRange : newDataRanges) {
			expressions.add(df.getOWLDataSomeValuesFrom(ce.getProperty(), newDataRange));
		}
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataAllValuesFrom ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataHasValue)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataHasValue ce) {
		Set<OWLClassExpression> expressions = new HashSet<>();
		expressions.add(ce);
		return expressions;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataMinCardinality)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataMinCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataExactCardinality)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataExactCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataMaxCardinality)
	 */
	@Nonnull
	@Override
	public Set<OWLClassExpression> visit(@Nonnull OWLDataMaxCardinality ce) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDatatype)
	 */
	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDatatype dr) {
		Set<OWLDataRange> dataRanges = new HashSet<>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataOneOf)
	 */
	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataOneOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataComplementOf)
	 */
	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataComplementOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataIntersectionOf)
	 */
	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataIntersectionOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataUnionOf)
	 */
	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDataUnionOf dr) {
		Set<OWLDataRange> dataRanges = new HashSet<>();
		dataRanges.add(dr);
		return dataRanges;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitorEx#visit(org.semanticweb.owlapi.model.OWLDatatypeRestriction)
	 */
	@Nonnull
	@Override
	public Set<OWLDataRange> visit(@Nonnull OWLDatatypeRestriction dr) {
		Set<OWLDataRange> dataRanges = new HashSet<>();
		Set<OWLFacetRestriction> facetRestrictions = dr.getFacetRestrictions();
		OWLLiteral min = null;
		OWLLiteral max = null;
		for (OWLFacetRestriction facetRestriction : facetRestrictions) {
			OWLFacet facet = facetRestriction.getFacet();
			if(facet == OWLFacet.MIN_INCLUSIVE){
				min = facetRestriction.getFacetValue();
			} else if(facet == OWLFacet.MAX_INCLUSIVE){
				max = facetRestriction.getFacetValue();
			} else {
				throw new IllegalArgumentException("Facet not allowed for transformation.");
			}
		}
//		dataRanges.add(dr);
		dataRanges.add(df.getOWLDatatypeRestriction(dr.getDatatype(), df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, min)));
		dataRanges.add(df.getOWLDatatypeRestriction(dr.getDatatype(), df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, max)));
//		dataRanges.add(dr.getDatatype());
		return dataRanges;
	}
	
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.MANCHESTER_SYNTAX);
		OWLDataFactoryImpl df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix(":");
		OWLClass A = df.getOWLClass("A", pm );
		OWLDataProperty s = df.getOWLDataProperty("s", pm);
		OWLDataProperty t = df.getOWLDataProperty("t", pm);
		OWLDatatypeRestriction dr1 = df.getOWLDatatypeMinMaxInclusiveRestriction(1.0, 2.0);
		OWLDatatypeRestriction dr2 = df.getOWLDatatypeMinMaxInclusiveRestriction(15, 100);
		OWLClassExpression ce = df.getOWLObjectIntersectionOf(A,
				df.getOWLDataSomeValuesFrom(s, dr1),
				df.getOWLDataSomeValuesFrom(t, dr2)						
				);
		Set<OWLClassExpression> expressions = ce.accept(new ClassExpressionLiteralCombination());
		for (OWLClassExpression expr : expressions) {
			System.out.println(expr);
		}
	}


}
