# Run all the integration tests

ROOT_DIRECTORY = .
TESTDIRS := ${sort ${dir ${wildcard ${ROOT_DIRECTORY}/*/Makefile}}}
where-am-i = $(CURDIR)/$(word ($words $(MAKEFILE_LIST)),$(MAKEFILE_LIST)

.PHONY: all $(TESTDIRS)

all: clean test

clean: $(TESTDIRS)

test: $(TESTDIRS)

$(TESTDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

#foo: baz
