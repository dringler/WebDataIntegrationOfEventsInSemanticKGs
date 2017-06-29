# Master Thesis Project
Git for Master Thesis Code

Data Integration of event data from the DBpedia and YAGO Knowledge Graphs using different blocking methods and matching rules.
Based on the <a href="https://github.com/olehmberg/winter">WInte.r - Web Data Integration Framework</a> (older version) and the 
<a href="http://l3s.de/~papadakis/erFramework.html">Blocking Framework</a> by Papadakis et al.
In addition, the <a href="http://silkframework.org/">SILK Linked Data Integration Framework</a> was used to learn different matching rules.

All blocking methods were tested on five different subsets (having different or additional attributes) to analyze the importance of different attributes.

Different consecutive blocking sub-tasks were tested:
<ol>
<li>Standard (token) blocking and Attribute Clustering for building the blocks</li>
<li>Block Filtering for cleaning the blocks</li>
<li>Meta-Blocking for cleaning comparisons</li>
</ol>

Tested parameters:
<ul>
<li>Standard (token) blocking: parameter-free</li>
<li>Attribute Clustering: five different representation models)</li>
<li>Block Filtering: 20 different ratios [0.05, 1.0] (steps of size 0.05) for the best of both block building methods</li>
<li>Meta-Blocking: five weighting schemes and four pruning algorithms for the best Block Filtering methods (for both block building methods)</li> 
</ul>

Main result: 
Blocking only becomes efficient when applying block- or comparison-refinement methods. 
For the analyzed data, taking all attributes for block building and removing entities from 50% of the largest blocks works best when regarding Pairs Completeness and Reduction Ratio. 
A simple matching rule that compares the stripped URI of the entities using a Levenshtein similarity measure with a high threshold of 0.96 outperforms all learned matching rules concerning the F-measure.

The full thesis is uploaded as thesis.pdf if you are interested in further details.
