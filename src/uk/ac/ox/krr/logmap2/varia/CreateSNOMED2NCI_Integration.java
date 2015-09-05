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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import uk.ac.ox.krr.logmap2.io.ReadFile;
//import deprecated.reasoning.HermiTReasonerAccess;

/**
 * 
 * @author Ernesto Jimenez
 *
 */
public class CreateSNOMED2NCI_Integration {

	
	private ReadFile reader;
	
	private Map<String, String> nci2snmd = new HashMap<String, String>();
	private OWLOntologyManager managerOWL_snmd = OWLManager.createOWLOntologyManager();
	private OWLOntologyManager managerOWL_nci = OWLManager.createOWLOntologyManager();
	private OWLOntologyManager managerOWL_mappings = OWLManager.createOWLOntologyManager();
	private OWLOntologyManager managerOWL_integration = OWLManager.createOWLOntologyManager();
	
	private OWLOntology snomed;
	private OWLOntology nci;
	private OWLOntology mappings;
	private OWLOntology integration;
	
	
	private List<OWLOntologyChange> listChangesIntegration = new ArrayList<OWLOntologyChange>();
	
	String snomed_iri;
	
	String nci_iri;
	
	String mappings_iri;
	
	
	public CreateSNOMED2NCI_Integration() throws Exception {
		
		String path = "/usr/local/data/DataUMLS/UMLS_Onto_Versions/LogMap2_Mappings/SNOMED2NCI_logmap2_Output/";
		
		//getSignatureSNOMED(path + "mappings.text");
		
		snomed_iri = "file:" + path + "module1.owl";
		
		nci_iri = "file:" + path + "module2.owl";
		
		mappings_iri = "file:" + path + "mappings.owl";
		
		loadNCI();
		
		//renameLUCADAClasses();
		
		loadMappings();
		
	
		loadSNOMED();
		
		
		saveIntegratedOntology();
	
	}
	
	
	
	
	private void getSignatureSNOMED(String mappingsFile) throws Exception{
		
		
		//Read from file
	
		reader = new ReadFile(mappingsFile);
		
		String line;
		String[] elements;
		
		line=reader.readLine();
		while (line!=null) {
			
			if (line.indexOf("|")<0){
				line=reader.readLine();
				continue;
			}
				
			elements=line.split("\\|");
			
			nci2snmd.put(elements[1], elements[0]);
			
			line=reader.readLine();
		}		
		
		reader.closeBuffer();
		
		
	
	}
	
	
	private void loadSNOMED() throws Exception {		
		System.out.println("Loading SNOMED...");
		snomed = managerOWL_snmd.loadOntology(IRI.create(snomed_iri));
		
		/*for (OWLClass cls : snomed.getClassesInSignature()) {
			System.out.println(cls.getIRI());
			break;
		}*/
		
	}
	
	private void loadMappings() throws Exception {		
		System.out.println("Loading Mappings...");
		mappings = managerOWL_mappings.loadOntology(IRI.create(mappings_iri));
		
		
	}
	
	private void loadNCI() throws Exception {		
		System.out.println("Loading NCI...");
		nci = managerOWL_nci.loadOntology(IRI.create(nci_iri));
	}
	
	
	private void saveIntegratedOntology() throws Exception {
		
		managerOWL_integration = OWLManager.createOWLOntologyManager();
		integration = managerOWL_integration.createOntology(nci.getOntologyID().getOntologyIRI());
		
		//Module SNOMED  Axioms
		for (OWLAxiom ax : snomed.getAxioms()) {
			listChangesIntegration.add(new AddAxiom(integration, ax));
		}

		//Lucada Axioms
		for (OWLAxiom ax : nci.getAxioms()) {
			listChangesIntegration.add(new AddAxiom(integration, ax));
		}
		
		//Mappings Axioms
		for (OWLAxiom ax : mappings.getAxioms()) {
			listChangesIntegration.add(new AddAxiom(integration, ax));
		}
		
		
		managerOWL_integration.applyChanges(listChangesIntegration);
		
		System.out.println("Storing merged ontology: " + integration.getClassesInSignature().size());
		
		managerOWL_integration.saveOntology(integration, new RDFXMLOntologyFormat(), 
				IRI.create("file:/usr/local/data/DataUMLS/UMLS_Onto_Versions/LogMap2_Mappings/SNOMED2NCI_logmap2_Output/snmd2nci_integration_with_mappings.owl"));
		
		
		
	}
	
	
	
	
	private void renameLUCADAClasses(){
		
		System.out.println("Renaming NCI...");
		
		List<OWLOntologyChange> listChanges = new ArrayList<OWLOntologyChange>();
		
		OWLEntityRenamer renamer = new OWLEntityRenamer(managerOWL_nci, managerOWL_nci.getOntologies()); 
		
		for (OWLClass cls : nci.getClassesInSignature()){
			
			if (!nci2snmd.containsKey(cls.getIRI().toString()))
				continue;
			
			listChanges.addAll(renamer.changeIRI(cls, IRI.create(nci2snmd.get(cls.getIRI().toString()))));
			
			
			managerOWL_nci.applyChanges(listChanges);
			
			listChanges.clear();
			
		}
		
	}
	
	
	public static void main(String[] args) {
		try {
			new CreateSNOMED2NCI_Integration();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
}
