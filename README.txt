Copyright 2013 by the Department of Computer Science (University of Oxford)

LogMap is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

LogMap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with LogMap.  If not, see <http://www.gnu.org/licenses/>.


LogMap is a highly scalable ontology matching system with ‘built-in’ reasoning and inconsistency repair capabilities. To the best of our knowledge, LogMap is the one of the few matching systems that (1) can efficiently match semantically rich ontologies containing tens (and even hundreds) of thousands of classes, (2) incorporates sophisticated reasoning and repair techniques to minimise the number of logical inconsistencies, and (3) provides support for user intervention during the matching process (this functionality is not provided in this standalone distribution of LogMap).

LogMap integrates the OWL API, HermiT, MORe and ELK reasoners which are under the LGPL or Apache licenses.


REQUIREMENTS:

Java 1.6 or higher.

The main LogMap's dependencies are the OWL API and the HermiT 1.3.8, which already bundles the OWL API 3.4.3 (see http://www.cs.ox.ac.uk/isg/tools/HermiT//download/1.3.8/). Additionally LogMap can also make use of MORe reasoner 0.1.6 (http://more-reasoner.googlecode.com/files/MORe.reasoner.0.1.6.zip) which, apart from HermiT, requires EL reasoner 0.4.1 (http://elk-reasoner.googlecode.com/files/elk-distribution-0.4.1-owlapi-library.zip)

Additionally, LogMap source codes also depend on other libraries (for extra functionalities), although they are not necessary for the "matching" and "repair" functionalities. 
See http://code.google.com/p/logmap-matcher/wiki/AboutLogMapSourceCodes for details.


USAGE:

LogMap2_Matcher.java and LogMap2_RepairFacility.java are the main classes.



RELEASE NOTES:

LogMap 2.4
------------
 * Uses OWL API 3.4.3 and HermiT 1.3.8
 * MORe reasoner 0.1.6, together with HermiT, has been integrated as candidate OWL 2 reasoner. Can be selected using the parameters file.
 * HermiT is now run ignoring non supported (OWL 2) datatypes.
 * Property matching algorithm exploits now inverses
 * Fixed issue with semantic index related to given negative preorder numbers.
 * Enhanced module extractor.
 * Class type for instances are now extracted using the reasoner.
 * Included "subtypes" case in InstanceAssesment class.
 * Considered stemming to discover weak instance mappings.
 * Instance mappings, apart from the compatibility score, also contains information about their scope (i.e. lexical similarity of their respective class types).
 * Update on the interactivity codes. Adaptation to participate in OAEI 2013.
 * Update on the instance matching module to cope with OAEI 2013 test case.
 * The Web interface has also been enhanced, mainly the issues related to ontologies outside the OWL 2 profile.
 * To avoid the use of non-real emails, the link to the results are only sent via email.
