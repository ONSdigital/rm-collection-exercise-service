diagrams: download-plantuml diagrams/collection-exercise-new-states.svg diagrams/collection-exercise-state.svg

clean:
	rm plantuml.jar; rm diagrams/*.svg

download-plantuml:
ifeq (,$(wildcard plantuml.jar))
	curl -L --output plantuml.jar https://downloads.sourceforge.net/project/plantuml/plantuml.jar
endif

%.svg : %.puml
	 java -jar plantuml.jar -tsvg $^

