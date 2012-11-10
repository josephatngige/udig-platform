/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2010, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Axios BSD
 * License v1.0 (http://udig.refractions.net/files/asd3-v10.html).
 */
package eu.udig.tools.parallel.internal;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Stores information about segments that intersects with others segments. It
 * only needs to store the start index of the related segment, his intersection
 * coordinate and if it intersects with a previous or next segment, this will be
 * the direction, isForward true for ahead segments and false for behind
 * segments.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.3
 */
final class DataSegmentIntersection {

	private int			startSegmentIndex;
	private Coordinate	intersectionCoord;
	private boolean		isForward;

	public DataSegmentIntersection(int startIndex, Coordinate intersectionCoord, boolean isForward) {

		assert intersectionCoord != null : "The coordinate can't be null"; //$NON-NLS-1$

		this.startSegmentIndex = startIndex;
		this.intersectionCoord = intersectionCoord;
		this.isForward = isForward;
	}

	public int getStartSegmentIndex() {

		return startSegmentIndex;
	}

	public Coordinate getIntersectionCoordinate() {

		return intersectionCoord;
	}

	public boolean getIsForward() {

		return isForward;
	}
}
