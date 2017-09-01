# blip

Bayesian network Learning and Inference Project

## References

This package implements the algorithms detailed in the following papers: 
* Learning Bounded-Treewidth Graphs with Thousands of Variables (submitted KDD 2017) - [supplementary material](supplementary-KDD17.pdf)
* Approximated Structural Learning for Large Bayesian Networks (submitted ML) - [supplementary material](supplementary-ML17.pdf)
* [Learning Treewidth-Bounded Bayesian Networks with Thousands of Variables](https://papers.nips.cc/paper/6232-learning-treewidth-bounded-bayesian-networks-with-thousands-of-variables) (NIPS 2016) Mauro Scanagatta, Giorgio Corani, Cassio P. de Campos, Marco Zaffalon

## Usage

The process of learning a bounded-treewidth BN is explained by using the "child" network as example.

### Dataset format

The format for the initial dataset has to be the same as the file "child-5000.dat", namely a space-separated file containing: 
* first line, list of variable names
* following lines, discrete value for each variable

### Common command line options

* -d VAL : Datafile path (.dat format)
* -j VAL : Parent set scores output file (.jkl format)
* -r VAL : Structure output file (.res format)
* -t N   : Maximum time limit, in seconds (default: 10)
* -b N   : Number of machine cores to use (default: 1)
* -w N   : Maximum treewidth
* -seed N   : Seed for the pseudo random number generator

### Parent set identification 

The first step is build the parent sets score cache. It can be done with: 
```
java -jar blip.jar scorer.sq -c bdeu -d data/child-5000.dat -j data/child-5000.jkl -n 3 -t 10
```

* -a N   : (if BDeu is chosen) equivalent sample size parameter (default: 1.0)
* -c VAL : Chosen score function. Possible choices: BIC, BDeu (default: bic)
* -n N   : Maximum learned in-degree (if 0, no constraint is applied) (default: 0)

### Bounded-treewidth structure optimization 

For perfoming with k-greedy: 

```
java -jar blip.jar solver.kg -j data/child-5000.jkl -r data/child.kg.res -t 10 -w 4 -v 1
```

For perfoming with the k-greedy enhanched by entropy-based sample ordering: 

```
java -jar blip.jar solver.kg.adv -smp ent -d data/child-5000.dat -j data/child-5000.jkl -r data/child-5000.kgent.res -t 10 -w 4 -v 1
```

For perfoming with k-max:

```
java -jar blip.jar solver.kmax -j data/child-5000.jkl -r data/child-5000.kmax.res -t 10 -w 4 -v 1
```
