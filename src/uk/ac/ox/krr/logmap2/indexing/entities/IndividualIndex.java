/*******************************************************************************
 * Copyright 2012 by the Department of Computer Science (University of Oxford)
 * 
 *    This file is part of LogMap.
 * 
 *    LogMap is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 * 
 *    LogMap is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 * 
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with LogMap.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package uk.ac.ox.krr.logmap2.indexing.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IndividualIndex  extends EntityIndex{
	
	protected Set<Integer> class_types = new HashSet<Integer>();
	
	private Set<String> alternativeLabels;
	
	
	public IndividualIndex(int i){
		
		index=i;
				
	}
	
	
	public Set<Integer> getClassTypes(){
		return class_types;
	}
	
	public void addClassTypeIndex(int icls){
		class_types.add(icls);
	}
	
	
	
	public boolean hasDirectClassTypes(){
		return !class_types.isEmpty();		
	}
	
	
	
	public void addAlternativeLabel(String altLabel){
		
		if (alternativeLabels==null)
			alternativeLabels=new HashSet<String>();
		
		alternativeLabels.add(altLabel);
		
	}
	
	public void setAlternativeLabels(Set<String> altLabels){
		
		alternativeLabels=new HashSet<String>(altLabels);
		
	}
	
	
	public void setEmptyAlternativeLabels(){
		
		alternativeLabels=new HashSet<String>();
		
	}
	
	
	
	
	/**
	 * Set of alternative labels or synonyms
	 * @return
	 */
	public Set<String> getAlternativeLabels(){
		if (alternativeLabels==null)
			return Collections.emptySet();
		
		return alternativeLabels;
	}
	
	public boolean hasAlternativeLabels(){
		if (alternativeLabels==null)
			return false;
		return true;		
	}
	
	
	

}
