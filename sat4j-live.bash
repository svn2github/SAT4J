#!/bin/bash
echo set multiplot>$1.gnuplot
echo set size 0.5, 0.5>>$1.gnuplot
echo set origin 0.0, 0.5>>$1.gnuplot
echo set title '"'Decision level at which the conflict occurs'"'>>$1.gnuplot
echo plot '"'$1-conflict-level.dat'"'>>$1.gnuplot
echo set size 0.5, 0.5>>$1.gnuplot
echo set origin 0.0, 0.0>>$1.gnuplot
echo set title '"'Index of the decision variables'"'>>$1.gnuplot
echo plot '"'$1-decision-indexes.dat'"'>>$1.gnuplot
echo set size 0.5, 0.5>>$1.gnuplot
echo set origin 0.5, 0.5>>$1.gnuplot
echo set title '"'Depth of the search when the conflict occurs'"'>>$1.gnuplot
echo plot '"'$1-conflict-depth.dat'"'>>$1.gnuplot
echo set size 0.5, 0.5>>$1.gnuplot
echo set origin 0.5, 0.0>>$1.gnuplot
echo set title '"'Size of the clause learned \(after minimization if any\)'"'>>$1.gnuplot
#echo set logscale y>>$1.gnuplot
echo plot '"'$1-learned-clauses-size.dat'"'>>$1.gnuplot
echo set nologscale y>>$1.gnuplot
echo unset multiplot>>$1.gnuplot
echo "pause 2">>$1.gnuplot
echo reread>>$1.gnuplot
gnuplot $1.gnuplot
