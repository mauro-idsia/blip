# blip

Bayesian network Learning and Inference Project

## References

This package implements the algorithms detailed in the following papers: 
* Learning Bounded-Treewidth Graphs with Thousands of Variables (submitted KDD 2017) - [supplementary material](supplementary-KDD17.pdf)
* Approximated Structural Learning for Large Bayesian Networks (submitted ML) - [supplementary material](supplementary-ML17.pdf)
* [Learning Treewidth-Bounded Bayesian Networks with Thousands of Variables](https://papers.nips.cc/paper/6232-learning-treewidth-bounded-bayesian-networks-with-thousands-of-variables) (NIPS 2016) Mauro Scanagatta, Giorgio Corani, Cassio P. de Campos, Marco Zaffalon - [supplementary material](supplementary-NIPS16.pdf)

## Usage

The process of learning a bounded-treewidth BN is explained by using the "child" network as example.

### Dataset format

The format for the initial dataset has to be the same as the file "child-5000.dat", namely a space-separated file containing: 
* first line, list of variable names
* following lines, discrete value for each variable

### Parent set identification 

The first step is build the parent sets score cache. It can be done with: 
```
java -jar blip.jar scorer.sq -c bdeu -d child-5000.dat -j child-5000.jkl -n 3 -t 10
```
Parameters: 

Task command line options: 
* -a N   : (if BDeu is chosen) equivalent sample size parameter (default: 1.0)
* -b N   : Number of machine cores to use (if 0, all are used) (default: 1) 
* -c VAL : Chosen score function. Possible choices: BIC, BDeu (default: bic)
*  -d VAL : Datafile path (.dat format)
* -j VAL : Parent set scores output file (jkl format)
* -n N   : Maximum learned in-degree (if 0, no constraint is applied) (default: 0)
* -t N   : Maximum time limit search for variable in seconds (if 0, no constraint is applied) (default: 0)
* -u VAL : Search only the selected variable (ex: '3' or '1-10')
* -v N   : Verbose level (default: 0)


### k-Greedy search 

Now from the scores cache we search the best bounded-treewidth BN (using k-greedy): 
```
java -jar blip.jar solver.kg -j child-5000.jkl -r child-5000.res -t 10 -w 4
```

Task command line options: 
* -b N   : number of machine cores to use (default: 1)
* -j VAL : Scores input file (in jkl format)
* -o N   : number of solutions to output (default: 1)
* -r VAL : result output file. If not supplied, the scores are printed on screen
* -t N   : maximum time limit (seconds) (default: 10)
* -v N   : Verbose level (default: 0)
* -w N   : maximum treewidth


### k-Greedy-ENT search 

For perfoming the search with the k-greedy with entropy-based sample ordering: 

```
java -jar blip.jar solver.kg -j child-5000.jkl -r child-5000.res -t 10 -w 4
```

