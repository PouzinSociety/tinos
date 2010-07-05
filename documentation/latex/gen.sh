#!/bin/bash

rm *.pdf
pdflatex booklet*.tex
pdflatex booklet*.tex
bibtex *.aux
pdflatex booklet*.tex
pdflatex booklet*.tex
cp *.pdf ../public
