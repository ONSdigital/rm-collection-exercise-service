DIAGRAM_DIR=diagrams
UML_FILES=$(wildcard $(DIAGRAM_DIR)/*.puml)
SVG_FILES := $(patsubst $(DIAGRAM_DIR)/%.puml,$(DIAGRAM_DIR)/%.svg,$(UML_FILES))

diagrams: download-plantuml $(SVG_FILES)

clean:
	rm plantuml.jar; rm diagrams/*.svg

download-plantuml:
ifeq (,$(wildcard plantuml.jar))
	curl -L --output plantuml.jar https://downloads.sourceforge.net/project/plantuml/plantuml.jar
endif

%.svg : %.puml
	 java -jar plantuml.jar -tsvg $^

