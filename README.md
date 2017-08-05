<img src="https://ischool.illinois.edu/sites/all/themes/gslis/images/gslis-logo-front.gif" width="200" alt="iSchool at Illinois"> 

# Evaluation Framework for Indri and Lucene


The ``ir-tools`` Java library provides a consistent framework for the comparative evaluation of information retrieval models using the [Indri](https://www.lemurproject.org/indri.php) or [Lucene](https://lucene.apache.org/) search engines. ``ir-tools`` implements a top-K rescoring model, whereby an initial retrieval using the underlying search engine is rescored. This allows for consistent retrieval model implementations regardless of the underlying search engine. ``ir-tools`` is maintained by researchers at the School of Information Sciences at the University of Illinois at Urbana-Champaign.

Key features:
* Consistent interfaces to Indri and Lucene indexes
* Top-K recoring 
* Baseline model from both language modeling and vector-space families
* Ability to configure separate background models when use of "future information" is a concern
* Evaluation tools for both ad-hoc and filtering tasks
* Relevance model and Rocchio expansion support
* Classes for the generic representation of feature vectors
* Utilities for conversion of queries and documents 
* JSON-based weighted query format

## Installation

### Pre-requisites

The following instructions assume an Ubuntu system running as root user:

```bash
apt-get update
apt-get install openjdk-8-jdk-headless maven
apt-get install r-base
apt-get install build-essential git parallel vim wget zlibc zlib1g zlib1g-dev
```

For Indri support, ``ir-tools`` requires the Indri system to be installed:

```bash
cd /usr/local/src
wget https://sourceforge.net/projects/lemur/files/lemur/indri-5.11/indri-5.11.tar.gz/download -O indri-5.11.tar.gz
tar xvfz indri-5.11.tar.gz
cd indri-5.11
./configure --enable-java --with-javahome=/usr/lib/jvm/java-8-openjdk-amd64
make 
make install
```
### From Maven Central

The ``ir-tools`` library is available from Maven Central.

### Building the ``ir-tools`` library
To build the ``ir-tools`` library:
```bash
https://github.com/uiucGSLIS/ir-tools.git
mvn package
```

## Usage Examples

Below are a few examples using the ``ir-tools`` library:

## Running Indri-style queries with Lucene

```
run.sh edu.gslis.lucene.main.LuceneRunQuery -index <path-to-index> -queryfile queries/<indri-query-file>  -format indri -field text -similarity method:dir,mu:2500 > <path-to-output>
```

## Generating RM3 queries
```
run.sh edu.gslis.biocaddie.util.GetFeedbackQueries -input queries/<query-file> -output <output-file> -index <index-path> -fbDocs <num-fbDocs> -fbTerms <num-fbTerms> -rmLambda <fbOrigWeight> -maxResults <top-K-num> -stoplist data/stoplist.all -mu <dirichlet mu>"
```

