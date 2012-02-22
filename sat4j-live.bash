#!/bin/bash
if [ $# == 2 ] ; then
    #echo "2 arguments : $1 et $2"
    pathToFichier="$2/$1"
else
    pathToFichier="$1"
fi
nbLignes=10000
echo $pathToFichier
echo set terminal x11>$pathToFichier.gnuplot
echo set multiplot>>$pathToFichier.gnuplot
echo set size 0.5, 0.5>>$pathToFichier.gnuplot
echo set origin 0.0, 0.5>>$pathToFichier.gnuplot
echo set title '"'Decision level at which the conflict occurs'"'>>$pathToFichier.gnuplot
echo plot  '"'$pathToFichier-conflict-level-restart.dat'"' with impulses ls 3,'"'$pathToFichier-conflict-level.dat'"' ls 1 >>$pathToFichier.gnuplot
echo set size 0.5, 0.5>>$pathToFichier.gnuplot
echo set origin 0.0, 0.0>>$pathToFichier.gnuplot
echo set title '"'Index of the decision variables'"'>>$pathToFichier.gnuplot
echo plot '"'$pathToFichier-decision-indexes-restart.dat'"' with impulses ls 3, '"'$pathToFichier-decision-indexes-pos.dat'"' ls 2, '"'$pathToFichier-decision-indexes-neg.dat'"' ls 1 >>$pathToFichier.gnuplot
echo set size 0.5, 0.5>>$pathToFichier.gnuplot
echo set origin 0.5, 0.5>>$pathToFichier.gnuplot
echo set title '"'Depth of the search when the conflict occurs'"'>>$pathToFichier.gnuplot
echo plot '"'$pathToFichier-conflict-depth.dat'"' >>$pathToFichier.gnuplot
echo set size 0.5, 0.5>>$pathToFichier.gnuplot
echo set origin 0.5, 0.0>>$pathToFichier.gnuplot
echo set title '"'Size of the clause learned \(after minimization if any\)'"'>>$pathToFichier.gnuplot
#echo set logscale y>>$1.gnuplot
echo plot '"'$pathToFichier-learned-clauses-size.dat'"' >>$pathToFichier.gnuplot
echo set nologscale y>>$pathToFichier.gnuplot
echo unset multiplot>>$pathToFichier.gnuplot
echo "pause 2">>$pathToFichier.gnuplot
echo reread>>$pathToFichier.gnuplot
gnuplot $pathToFichier.gnuplot
