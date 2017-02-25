# The pizza slicing problem #
This is my personal solution for the 2017 exercise of the Google Hashcode competition.

## How to build and run ##
You'll need Maven to build the project.

Fork the repository, then run mvn:test to launch the tests.
You'll find the results in the target/test-classes directory as ${datasetname}.out.

## Working principle ##
This is a greedy algorithm based on a weighted tree.

It starts from the first top-left free cell of the pizza and starts
exploring all possible feasible combinations, moving the selection either
right or down, until the size of the slice is too big, an edge is encountered,
or the selection is invalid because it contains a cell that has already been
cut (so it's part of another slice thus overlapping slices).

Every combination of start cell - end cell (hereby called a selection)
is then assigned a weight, that may vary from minus infinite (if the solution
is not correct such as a solution where there aren't enough toppings to
reach the minimum required, the selection is just one cell, or the selection
is over the maximum allowed size) to any decimal number, positive or negative,
depending on the current and previous selection.

The choice of the weight is influenced by the following constraints:
* **"Minimum Ingredients" Constraint:** Going towards the minimum number of
ingredients required will influence the score positively (this will assign
one point for every ingredient type that helps towards the goal)
* **"Removal of the Unwanted" Constraint:** Removing topping that are in excess
will influence the score positively by the ratio of topping that has been
removed (say that in an iteration 2/3 of exceeding Tomatoes have been
removed, then the score will increase of 0.6)
* **"Mourning of the Best" Constraint** Similarly, removing topping that is
not in excess (thus resulting in reducing the total number of slices that
can be made) will influence the score negatively by the same ratio of topping
that has been removed (say that in an iteration 2/3 of Mushrooms have been
removed, but we were lacking in them, will result in a score decrease of 0.6)
* **"Died for Better Cause" Constraint:** If the scarce topping is removed,
but it was needed to reach the minimum topping quantity on a slice,
score is still increased, even if by a lesser quantity.
* **"Forced Path" Constraint** If we're going in the only possible direction, e.g. because we've reached
the frontier of the pizza, even if we're reducing the ingredient that we're
short with, we are actually avoiding waste, so we're going towards a better
solution

After all the possible nodes have been built, a slice is created choosing the
best fit (the node with the highest score). The corresponding cells (toppings)
are removed from the pizza to avoid overlapping on the next slice iteration.

The following iteration takes then the first available node in the top-left
cell of the pizza.

## Changelog ##

### V 1.2 ###
After submitting the code and verifying that some slices overlapped,
added a new constraint that avoids exploring nodes where slices overlap.

Also, fixed some typos in this document.

### V 1.1 ###
Added the FPC

### V 1.0 ###
Base version of the algorithm

## Results ##

### Example set ###
The example set has been used as test suite to ensure the solution is
optimal. 100% of the cells are used, in 3 slices, divided as the
original solution provided

### Small set ###
Without the latest condition added in 1.1, the small data set test
ran with 90.48% of cells used.

Some cells that should have been used
were left to waste, so the weighing system was modified to take into account
the "Forced Path Constraint (FPC)" as described above. Waste was reduced to
2 cells, with a score of 95.24% of pizza used.

### Medium set ###
The test runs in under a second, with 96.6% of cells used and 4'442 slices
produced.

#### Version 1.1 ####
With FPC, cells used are 97.82%, 4510 slices produced.

#### Version 1.2 ####
Previous versions created overlapped slices, so the algorithm has been refined
to remove this unwanted occurrence. In this version the medium data set results
worsened to 96.7%.

### Big set ###
The test takes quite a while to run (can vary based on CPU speed, from
1 minute to more than 3) and has a score of 89.25% of used cells, with
65'817 slices produced. Even if the percentage is quite good, 107'501 cells
are marked were wasted.

#### Version 1.1 ####
Adding FPC improved the score to 89.67%, waste was reduced to 103'302 cells:
4'199 cells were saved from waste, producing 66'148 slices, 331 more than the
previous run.

#### Version 1.2 ####
Removing overlapping slices worsened the result to 89.18% of pizza used

## Credits ##
* First release, idea and code by me.