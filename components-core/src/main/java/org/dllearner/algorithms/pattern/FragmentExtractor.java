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
package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.OWLClass;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public interface FragmentExtractor {
	
	/**
	 * Extracts a fragment of the knowledge base for the given class C with a max. depth of of triples starting from
	 * instances of C.
	 * @param cls the class
	 * @param maxFragmentDepth the maximum depth
	 * @return the fragment
	 */
	Model extractFragment(OWLClass cls, int maxFragmentDepth);

}
