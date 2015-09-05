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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import uk.ac.manchester.syntactic_locality.ModuleExtractor;
import uk.ac.ox.krr.logmap2.io.ReadFile;
import uk.ac.ox.krr.logmap2.reasoning.HermiTAccess;
import uk.ac.ox.krr.logmap2.utilities.Utilities;

/**
 * 
 * @author Ernesto Jimenez
 *
 */
public class CreateIntegratedLUCADA {

	
	private ReadFile reader;
	
	private Set<OWLEntity> signatureSNOMED = new HashSet<OWLEntity>();
	
	private Map<String, String> lucada2snmd = new HashMap<String, String>();
	private Map<String, String> snmd2lucada = new HashMap<String, String>();
	
	private OWLOntologyManager managerOWL_snmd = OWLManager.createOWLOntologyManager();
	private OWLOntologyManager managerOWL_lucada = OWLManager.createOWLOntologyManager();
	private OWLOntologyManager managerOWL_integration = OWLManager.createOWLOntologyManager();
	
	private OWLOntology snomed;
	private OWLOntology snomed_module;
	private OWLOntology lucada;
	private OWLOntology integration;
	
	private ModuleExtractor snomed_extractor;
	
	private List<OWLOntologyChange> listChangesIntegration = new ArrayList<OWLOntologyChange>();
	
	String snomed_iri;
	
	String lucada_iri;
	
	private String rdf_label_uri = "http://www.w3.org/2000/01/rdf-schema#label";
	private String purl_ident_iri = "http://purl.org/dc/elements/1.1/identifier";
	
	
	
	
	
	public CreateIntegratedLUCADA() throws Exception {
		
		getSignatureSNOMED("/usr/local/data/DataUMLS/UMLS_Onto_Versions/LUCADA/references_to_snomed_sct.txt");
		
		snomed_iri = "file:/usr/local/data/DataUMLS/UMLS_Onto_Versions/LUCADA/snomed20110131_replab_with_ids2.owl";
		
		lucada_iri = "file:/usr/local/data/DataUMLS/UMLS_Onto_Versions/LUCADA/LUCADAOntology15Feb2012.owl";
		
		loadLucada();
		
		renameLUCADAClasses();
	
		loadSNOMED();
		
		extractSNOMEDModule();
		
		createIntegratedOntology();
		
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
			
			lucada2snmd.put(elements[1], elements[0]);
			snmd2lucada.put(elements[0], elements[1]);
			
			if (elements.length==2)
				signatureSNOMED.add(OWLManager.getOWLDataFactory().getOWLClass(IRI.create(elements[0])));
			else{ //3 elements
				if (elements[2].equals("ObjectProperty")){
					signatureSNOMED.add(OWLManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(elements[0])));
				}
				else{//Data
					signatureSNOMED.add(OWLManager.getOWLDataFactory().getOWLDataProperty(IRI.create(elements[0])));
				}
			}
			

			line=reader.readLine();
		}		
		
		reader.closeBuffer();
		
		
	
	}
	
	
	private void loadSNOMED() throws Exception {		
		System.out.println("Loading SNOMED...");
		snomed = managerOWL_snmd.loadOntology(IRI.create(snomed_iri));
		
		for (OWLClass cls : snomed.getClassesInSignature()) {
			System.out.println(cls.getIRI());
			break;
		}
		
	}
	
	private void loadLucada() throws Exception {		
		System.out.println("Loading LUCADA...");
		lucada = managerOWL_lucada.loadOntology(IRI.create(lucada_iri));
	}

	
	
	private void createIntegratedOntology() throws Exception {
		
		
		Set<OWLAxiom> setAxioms = new HashSet<OWLAxiom>();
		setAxioms.addAll(snomed_module.getAxioms());
		
		setAxioms.addAll(lucada.getAxioms());
				
		managerOWL_integration = OWLManager.createOWLOntologyManager();
		//integration = managerOWL_integration.createOntology(lucada.getOntologyID().getOntologyIRI());
		
		integration = managerOWL_integration.createOntology(setAxioms, lucada.getOntologyID().getOntologyIRI());
		
		
		OWLEntityRenamer renamer = new OWLEntityRenamer(managerOWL_integration, managerOWL_integration.getOntologies());
		
		boolean hasLabel;
		
		//We come back to LUCADA URI
		//for (OWLClass cls : lucada.getClassesInSignature()){
		for (OWLEntity ent : integration.getSignature()){
			
			if (!snmd2lucada.containsKey(ent.getIRI().toString()))
				continue;
			
			listChangesIntegration.addAll(renamer.changeIRI(ent, IRI.create(snmd2lucada.get(ent.getIRI().toString()))));
			
			managerOWL_lucada.applyChanges(listChangesIntegration);
			
			listChangesIntegration.clear();
			
		}
		
		String label_value="";
		
		for (OWLEntity ent : integration.getSignature()){
			
			hasLabel=false;
			
			//If no label
			//Otherwise We look for label first (if no label we keepID)
			for (OWLAnnotationAssertionAxiom annAx : ent.getAnnotationAssertionAxioms(integration)){
				
				//listchanges.add(new RemoveAxiom(onto, annAx)); //We remove all annotations
				
				if (annAx.getAnnotation().getProperty().getIRI().toString().equals(rdf_label_uri)){
					
					hasLabel=true;
					
					label_value=((OWLLiteral)annAx.getAnnotation().getValue()).getLiteral();//.toLowerCase();
					//We remove current label because the "english" ann gives error for rendering
					listChangesIntegration.add(new RemoveAxiom(integration, annAx));
					
					
				}

			}
			
			if (!hasLabel){
				
				label_value = Utilities.getEntityLabelFromURI(ent.getIRI().toString());
						
			}
			
			listChangesIntegration.add(new AddAxiom(
						integration, 
						OWLManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(
								OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(rdf_label_uri)), 
								IRI.create(ent.getIRI().toString()), 
								OWLManager.getOWLDataFactory().getOWLLiteral(
								label_value)))
						);
			
			
			managerOWL_lucada.applyChanges(listChangesIntegration);
			
			listChangesIntegration.clear();

			
			
			
		
		}
		
		
		//Module SNOMED  Axioms
		/*for (OWLAxiom ax : snomed_module.getAxioms()) {
			listChangesIntegration.add(new AddAxiom(integration, ax));
		}

		//Lucada Axioms
		for (OWLAxiom ax : lucada.getAxioms()) {
			listChangesIntegration.add(new AddAxiom(integration, ax));
		}	
		managerOWL_integration.applyChanges(listChangesIntegration);*/
		
		
		
	}
	
	
	private void saveIntegratedOntology() throws Exception {
		
	System.out.println("Storing merged ontology: " + integration.getClassesInSignature().size());
		
		managerOWL_integration.saveOntology(integration, new RDFXMLOntologyFormat(), 
				IRI.create("file:/usr/local/data/DataUMLS/UMLS_Onto_Versions/LUCADA/lucada_snomed_march2012.owl"));
		
		//Classify Ontology and getting errors
		HermiTAccess reasonerMerged = new HermiTAccess(managerOWL_integration,
				integration, false);
		Set<OWLClass> unsatisfiableClasses = reasonerMerged.getUnsatisfiableClasses();
		System.out.println("There are '" + unsatisfiableClasses.size() + "' unsatisfiable classes.");
		
		
	}
	
	
	private void extractSNOMEDModule(){
		System.out.println("Extracting SNOMED module...");
		//Bottom locality, but extracting annotations
		snomed_extractor = new ModuleExtractor(snomed, false, false, false, true, false);
		System.out.println(signatureSNOMED.size());
		snomed_module = snomed_extractor.getLocalityModuleForSignatureGroup(signatureSNOMED);
		System.out.println(snomed_module.getClassesInSignature().size());
	
	}
	
	
	private void renameLUCADAClasses(){
		
		System.out.println("Renaming LUCADA...");
		
		List<OWLOntologyChange> listChanges = new ArrayList<OWLOntologyChange>();
		
		OWLEntityRenamer renamer = new OWLEntityRenamer(managerOWL_lucada, managerOWL_lucada.getOntologies()); 
		
		//for (OWLClass cls : lucada.getClassesInSignature()){
		for (OWLEntity ent : lucada.getSignature()){
			
			if (!lucada2snmd.containsKey(ent.getIRI().toString()))
				continue;
			
			listChanges.addAll(renamer.changeIRI(ent, IRI.create(lucada2snmd.get(ent.getIRI().toString()))));
			
			managerOWL_lucada.applyChanges(listChanges);
			
			listChanges.clear();
			
			//We add original lucada name
			/*listChanges.add(new AddAxiom(
					lucada, 
					OWLManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(
							OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(rdf_label_uri)), 
							IRI.create(lucada2snmd.get(cls.getIRI().toString())), 
							OWLManager.getOWLDataFactory().getOWLLiteral("Original LUCADA label: "+ Utilities.getEntityLabelFromURI(cls.getIRI().toString()))))
							);
			*/
			
			listChanges.add(new AddAxiom(
					lucada, 
					OWLManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(
							OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(purl_ident_iri)), 
							IRI.create(lucada2snmd.get(ent.getIRI().toString())), 
							OWLManager.getOWLDataFactory().getOWLLiteral(lucada2snmd.get(ent.getIRI().toString()))))
							);
			
			managerOWL_lucada.applyChanges(listChanges);
			
			listChanges.clear();
			
		}
		
	}
	
	
	public static void main(String[] args) {
		try {
			new CreateIntegratedLUCADA();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
}
