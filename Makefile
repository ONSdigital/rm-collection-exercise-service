DOT := $(shell command -v dot 2> /dev/null)

diagrams: ensure-graphviz download-plantuml
	java -jar plantuml.jar -tsvg diagrams/*.puml

clean:
	rm -f plantuml.jar; rm -f diagrams/*.svg

download-plantuml:
ifeq (,$(wildcard plantuml.jar))
	curl -L --output plantuml.jar https://downloads.sourceforge.net/project/plantuml/plantuml.jar
endif 

ensure-graphviz:
ifndef DOT
	$(error "The dot command is not available - please install graphviz (brew install graphviz)")
endif
