# Run a test
#
.PHONY: all test

DIRNAME=$(shell basename $(shell realpath $(CURDIR)))

TEST = $(DIRNAME).sh

all: clean test

test: datadir
	@./$(TEST)

datadir:
	@./$(TEST) --dataset

clean:
	rm -rf datadir

