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
package uk.ac.ox.krr.logmap2.varia;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import uk.ac.ox.krr.logmap2.io.WriteFile;
//import deprecated.reasoning.HermiTReasonerAccess;
import uk.ac.ox.krr.logmap2.reasoning.HermiTAccess;
import uk.ac.ox.krr.logmap2.reasoning.StructuralReasonerExtended;


public class OWLOntology2FunctionalSyntax {
	
	private OWLOntologyManager managerOnto;
	private OWLDataFactory factory;
	private OWLOntology onto;
	
	private String onto_iri;
	private String onto_iri_funct_syn;
	private String condor_query_file;
	
	
	long init, fin;
	
	
	WriteFile writer;
	
	private OWLReasoner reasoner;
	
	
	/**
	 * 
	 * @param outputIRI
	 * @param inputIRI
	 * @throws Exception
	 */
	public OWLOntology2FunctionalSyntax(
			String outputIRI, 
			String inputIRI, 
			boolean addEquivAxioms,
			String queryFilePath) throws Exception{
		
		onto_iri=inputIRI;
		onto_iri_funct_syn=outputIRI;
		condor_query_file=queryFilePath;
		
		//Load Ontology
		init=Calendar.getInstance().getTimeInMillis();
		loadOntology();
		fin = Calendar.getInstance().getTimeInMillis();
		System.out.println("Time loading ontology (s): " + (float)((double)fin-(double)init)/1000.0);
		
		
		//Add additional equivalence axioms (optional)
		if (addEquivAxioms) {
			init=Calendar.getInstance().getTimeInMillis();
			addNewNegatedAxioms();
			fin = Calendar.getInstance().getTimeInMillis();
			System.out.println("Time adding new axioms (s): " + (float)((double)fin-(double)init)/1000.0);
		}
		
		init=Calendar.getInstance().getTimeInMillis();
		setUpReasoner(false);
		fin = Calendar.getInstance().getTimeInMillis();
		System.out.println("Time classifying onto (s): " + (float)((double)fin-(double)init)/1000.0);
		
		//Create condor query "A and B" -> ??
		//Will detect disjointness
		init=Calendar.getInstance().getTimeInMillis();
		//createConjuntiveQueries4Condor();
		fin = Calendar.getInstance().getTimeInMillis();
		System.out.println("Create file with conjuncts (s): " + (float)((double)fin-(double)init)/1000.0);
		
		
				
		//Save to functional syntax
		init=Calendar.getInstance().getTimeInMillis();
		//saveOntology();
		fin = Calendar.getInstance().getTimeInMillis();
		System.out.println("Time saving ontology (s): " + (float)((double)fin-(double)init)/1000.0);
		
		
	}
	
	
	private void loadOntology() throws Exception {		

		managerOnto = OWLManager.createOWLOntologyManager();
		
		factory = managerOnto.getOWLDataFactory();
	
		//If import cannot be loaded
		managerOnto.setSilentMissingImportsHandling(true);
		
		onto = managerOnto.loadOntology(IRI.create(onto_iri));
		
		
		
		Set<OWLOntology> set4Module = new HashSet<OWLOntology>();
		
		set4Module.add(onto);
		
		DLExpressivityChecker checker = new DLExpressivityChecker(set4Module);
		
		System.out.println("Expressivity Ontology: " + checker.getDescriptionLogicName());
		set4Module.clear();
		
		 
		//signatureOnto = onto.getClassesInSignature();
		//signatureOnto1.add(factory.getOWLNothing());
		 
	}
	
	
	
	
	
	
	private void addNewNegatedAxioms(){
		
		System.out.println(onto.getAxiomCount());
		System.out.println(onto.getClassesInSignature().size());
				
		List<OWLOntologyChange> listequivalences= new ArrayList<OWLOntologyChange>();
		
		for (OWLClass cls : onto.getClassesInSignature()){
						
				
			/*listequivalences.add(					
					new AddAxiom(
							onto,
							factory.getOWLEquivalentClassesAxiom(
									factory.getOWLClass(IRI.create(cls.getIRI().toString()+"Negated")),
									factory.getOWLObjectComplementOf(cls)									
							)													
					)								
			);*/
			
			
			listequivalences.add(					
			new AddAxiom(
					onto,
					factory.getOWLSubClassOfAxiom(							
							factory.getOWLObjectComplementOf(cls),
							factory.getOWLClass(IRI.create(cls.getIRI().toString()+"Negated"))
					)													
				)								
			);
			
			
		
		}		
		
		managerOnto.applyChanges(listequivalences);
		
		System.out.println(onto.getAxiomCount());
		System.out.println(onto.getClassesInSignature().size());
		
	}
	
	
	private void createConjuntiveQueries4Condor(){
		
		writer=new WriteFile(condor_query_file);
		
		
		//NodeSet<OWLClass> nodeSet = reasoner.getSubClasses(reasoner.getTopClassNode().getRepresentativeElement(), false);
		
		//reasoner.getDisjointClasses(reasoner.getTopClassNode().getRepresentativeElement()).c
		
		
		OWLClass[] classesArray = new OWLClass[onto.getClassesInSignature().size()];
		classesArray = onto.getClassesInSignature().toArray(classesArray);
		
		for (int i=0; i < classesArray.length; i++){
			for (int j=i+1; j < classesArray.length; j++){
				
				//if (reasoner.getSubClasses(classesArray[i], false).getFlattened().contains(classesArray[j]) ||
				//	reasoner.getSubClasses(classesArray[j], false).getFlattened().contains(classesArray[i])){ //||
				if (	reasoner.getDisjointClasses(classesArray[i]).containsEntity(classesArray[j]) ||
					reasoner.getDisjointClasses(classesArray[j]).containsEntity(classesArray[i])){
					
					continue;
				}
				//else {	
					writer.writeLine("<"+ classesArray[i].getIRI().toString() + "> <" + classesArray[j].getIRI().toString() + ">");
				//}
				
			}			
		}			
		
		writer.closeBuffer();
		
	}
	
	
	
	
	private void setUpReasoner(boolean structural) throws Exception {
		
		if (structural){
			//StructuralReasonerFactory structReasonerFactory;
			//structReasonerFactory = new StructuralReasonerFactory();
			//reasoner = structReasonerFactory.createNonBufferingReasoner(onto);
			reasoner = new StructuralReasonerExtended(onto);
			//((StructuralReasonerExtended)reasoner).prepareReasoner();
			
		}
		else { //HermiT by default
			//OWLReasoner reasoner=new ReasonerFactory().createReasoner(onto);
			HermiTAccess hermit = new HermiTAccess(managerOnto, onto, false);
			reasoner = hermit.getReasoner();			
			//hermit.getClassifiedOntology();
			//for (OWLClass cls : onto.getClassesInSignature()){
			//	hermit.getReasoner().getDisjointClasses(cls);
			//}
			
			// hermit.craeateInferredDisjointAxioms();
			
		}
	}
	
	
	

	
	
	
	
	
	private void saveOntology() throws Exception{
		managerOnto.saveOntology(onto, new OWLFunctionalSyntaxOntologyFormat(), IRI.create(onto_iri_funct_syn));
	}
	
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		try {
			
			OWLOntology2FunctionalSyntax index;
			int onto=7;
			
			//String rootpath = "/usr/local/data/DataUMLS/UMLS_Onto_Versions/";
			String rootpath = "c://Users/ernesto/EclipseWS/DataUMLS/UMLS_Onto_Versions/";
			
			//String uriPath = "file:" + rootpath;
			String uriPath = "file:/" + rootpath;
			
			boolean addNegatedAxioms=false;
			
			String negatedEqStr  ="";
			
			if (addNegatedAxioms)
				negatedEqStr="_NegatedClasses";
			
			if (onto==1) {
			
				index = new OWLOntology2FunctionalSyntax(
						uriPath + "NCI_Thesaurus_08.05d" + negatedEqStr + "_funct_syntax.owl",
						uriPath + "NCI_Thesaurus_08.05d_cleant.owl",
						addNegatedAxioms,
						rootpath + "condor_query_" + "NCI_Thesaurus_08.05d" + ".txt");
			}
			else if (onto==2){
			
				index = new OWLOntology2FunctionalSyntax(
						uriPath + "FMADL_cleant_2_0" + negatedEqStr + "_funct_syntax.owl",
						uriPath + "FMADL_cleant_2_0.owl",
						addNegatedAxioms,
						rootpath + "condor_query_" + "FMADL_cleant_2_0" + ".txt");
			}
			else if (onto==3){
				index = new OWLOntology2FunctionalSyntax(
						uriPath + "snomed20090131_replab" + negatedEqStr + "_funct_syntax.owl",
						uriPath + "snomed20090131_replab.owl",
						addNegatedAxioms,
						rootpath + "condor_query_" + "snomed20090131_replab" + ".txt");
				
			}
			
			else if (onto==4){
				
				index = new OWLOntology2FunctionalSyntax(
						uriPath + "OntosRobotics/ChemoSupportv2_labels" + negatedEqStr + "_funct_syntax.owl",
						uriPath + "OntosRobotics/ChemoSupportv2_labels.owl",
						addNegatedAxioms,
						rootpath + "OntosRobotics/condor_query_" + "ChemoSupportv2_labels" + ".txt");
			}
			
			
			else if (onto==5){
				
				index = new OWLOntology2FunctionalSyntax(
						uriPath + "Anatomy/mouse_anatomy_2010" + negatedEqStr + "_funct_syntax.owl",
						uriPath + "Anatomy/mouse_anatomy_2010.owl",
						addNegatedAxioms,
						rootpath + "Anatomy/condor_query_" + "mouse_anatomy_2010" + ".txt");
			}
			
			else if (onto==6){
				
				index = new OWLOntology2FunctionalSyntax(
						uriPath + "Anatomy/nci_anatomy_2010" + negatedEqStr + "_funct_syntax.owl",
						uriPath + "Anatomy/nci_anatomy_2010.owl",
						addNegatedAxioms,
						rootpath + "Anatomy/condor_query_" + "nci_anatomy_2010" + ".txt");
			}
			
			
			else if (onto==7){
				
				index = new OWLOntology2FunctionalSyntax(
						uriPath + "Anatomy/nci_anatomy_2010_nodisj" + negatedEqStr + "_funct_syntax.owl",
						uriPath + "Anatomy/nci_anatomy_2010_nodisj.owl",
						addNegatedAxioms,
						rootpath + "Anatomy/condor_query_" + "nci_anatomy_2010" + ".txt");
			}
			
			
						
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
		
		


}
