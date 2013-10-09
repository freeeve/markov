## markov chains in neo4j
an example unmanaged extension for some markov chain operations.

### usage example:
/markov/chains returns an array of all paths up to a length (node ids), and their probability
/markov/results returns an array of start/end node ids, and their probability (sum of all paths)

You give them a cypher statement to get the start nodes, and a length.

#### graph looks like this:
```
CREATE (Rain {name:'Rain'}), (NoRain {name:'NoRain'}),
       (Rain)-[:PROB {p:0.7}]->(Rain),
       (Rain)-[:PROB {p:0.3}]->(NoRain),
       (NoRain)-[:PROB {p:0.4}]->(Rain),
       (NoRain)-[:PROB {p:0.6}]->(NoRain)
```

#### curl output:
``` shell
$ curl -X POST -H Content-Type:application/json \
  -d '{"lookup":"MATCH n WHERE n.name in [\"Rain\", \"NoRain\"] RETURN n", "length":4}' \
   http://localhost:7474/markov/chains
[{"path":[1,2,1,2,1],"prob":0.0144},
 {"path":[1,2,1,2,2],"prob":0.0336},
 {"path":[1,2,1,1,2],"prob":0.0288},
 {"path":[1,2,1,1,1],"prob":0.043199999999999995},
 {"path":[1,2,2,1,2],"prob":0.0336},
 {"path":[1,2,2,1,1],"prob":0.05039999999999999},
 {"path":[1,2,2,2,1],"prob":0.05879999999999999},
 {"path":[1,2,2,2,2],"prob":0.1372},
 {"path":[1,1,2,1,2],"prob":0.0288},
 {"path":[1,1,2,1,1],"prob":0.043199999999999995},
 {"path":[1,1,2,2,1],"prob":0.05039999999999999},
 {"path":[1,1,2,2,2],"prob":0.11759999999999998},
 {"path":[1,1,1,2,1],"prob":0.043199999999999995},
 {"path":[1,1,1,2,2],"prob":0.10079999999999999},
 {"path":[1,1,1,1,2],"prob":0.0864},
 {"path":[1,1,1,1,1],"prob":0.1296},
 {"path":[2,1,2,1,2],"prob":0.0144},
 {"path":[2,1,2,1,1],"prob":0.021599999999999998},
 {"path":[2,1,2,2,1],"prob":0.025199999999999997},
 {"path":[2,1,2,2,2],"prob":0.05879999999999999},
 {"path":[2,1,1,2,1],"prob":0.021599999999999998},
 {"path":[2,1,1,2,2],"prob":0.05039999999999999},
 {"path":[2,1,1,1,2],"prob":0.0432},
 {"path":[2,1,1,1,1],"prob":0.0648},
 {"path":[2,2,1,2,1],"prob":0.0252},
 {"path":[2,2,1,2,2],"prob":0.0588},
 {"path":[2,2,1,1,2],"prob":0.0504},
 {"path":[2,2,1,1,1],"prob":0.0756},
 {"path":[2,2,2,1,2],"prob":0.05879999999999999},
 {"path":[2,2,2,1,1],"prob":0.08819999999999997},
 {"path":[2,2,2,2,1],"prob":0.10289999999999998},
 {"path":[2,2,2,2,2],"prob":0.24009999999999992}]

$ curl -X POST -H Content-Type:application/json \
  -d '{"lookup":"MATCH n WHERE n.name in [\"Rain\", \"NoRain\"] RETURN n", "length":4}' \
   http://localhost:7474/markov/results
[{"start":2,"end":1,"prob":0.4250999999999999},
 {"start":2,"end":2,"prob":0.5749},
 {"start":1,"end":1,"prob":0.4332},
 {"start":1,"end":2,"prob":0.5668}]

$ curl -X POST -H Content-Type:application/json \
  -d '{"lookup":"MATCH n WHERE n.name in [\"Rain\", \"NoRain\"] RETURN n", "length":1}' \
   http://localhost:7474/markov/results
[{"start":2,"end":1,"prob":0.3},
 {"start":2,"end":2,"prob":0.7}
 {"start":1,"end":1,"prob":0.6},
 {"start":1,"end":2,"prob":0.4}]
 
$ curl -X POST -H Content-Type:application/json \
  -d '{"lookup":"MATCH n WHERE n.name in [\"Rain\", \"NoRain\"] RETURN n", "length":20}' \
   http://localhost:7474/markov/results
[{"start":2,"end":1,"prob":0.42857142855615876},
 {"start":2,"end":2,"prob":0.5714285714436717},
 {"start":1,"end":1,"prob":0.4285714285910035},
 {"start":1,"end":2,"prob":0.571428571407668}]
```

### configuration

Add this line to the end of your neo4j-server.properties file:
`org.neo4j.server.thirdparty_jaxrs_classes=markov=/markov`

Copy from dist `markov_2.10-0.1.jar`, `lift-json_2.10-2.5.jar` and `paranamer-2.4.1.jar` into the `lib/` folder in your neo4j server installation.
