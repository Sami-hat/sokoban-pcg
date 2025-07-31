# sokoban-pcg
A procedural Content Generation Tool for Sokoban

My Dissertation Project, exploring PCG for a PSPACE-COMPLETE Problem - Sokoban

# Compile Java code
javac -d out $(find src -name "*.java")

# Play set of levels by running the GUI and selecting a 'levelset' number in the prompt
# '-1' for a random (not pre-generated) level based on the current settings of the generator
# '0' for the default levelset
# '1' for the top 10 composite-rating levels, used for in Section 6.3.2 
# '2' for testing random individual metrics 
./run_gui.sh

# Run the generator once
./run_gen.sh

# Runs generation in bulk 
./run_solutions.sh
