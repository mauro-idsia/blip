# blip

Bayesian network Learning and Inference Project

## References

This package implements the algorithms detailed in the following papers: 
* Efficient learning of bounded-treewidth Bayesian networks from complete and incomplete data sets(https://www.sciencedirect.com/science/article/pii/S0888613X17307272) (IJAR 2018) - [supplementary material](supplementary-IJAR.pdf)
* Approximated Structural Learning for Large Bayesian Networks (accepted for publication ML 2018) - [supplementary material](supplementary-ML17.pdf)
* [Learning Treewidth-Bounded Bayesian Networks with Thousands of Variables](https://papers.nips.cc/paper/6232-learning-treewidth-bounded-bayesian-networks-with-thousands-of-variables) (NIPS 2016) Mauro Scanagatta, Giorgio Corani, Cassio P. de Campos, Marco Zaffalon

## Usage

The process of learning a bounded-treewidth BN is explained by using the "child" network as example.

### Dataset format

The format for the initial dataset has to be the same as the file "child-5000.dat", namely a space-separated file containing: 

    * First line: list of variables names, separated by space;
    * Second line: list of variables cardinalities, separated by space;
    * Following lines: list of values taken by the variables in each datapoint, separated by space.

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

### Interpreting the result 

The format of the ".res" file is as follows: each line indicates the parent set assigned to each variable and its score.

For example the line "4: -2797.39 (10,17,18)" indicates that to the variable with index 4 in the dataset are assgined as parents the variables with index (10,17,18). This parent set has score -2797.39 (by default the score function is the BIC). 

### Learn the parameters

Using the structure found it is possible to learn the parameters with: 

```
java -jar blip.jar parle -d data/child-5000.dat -r data/child-5000.kmax.res -n data/child-5000.kmax.uai
```

The final output will be a full Bayesian network in UAI format. 
