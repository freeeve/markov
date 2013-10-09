## markov chains in neo4j
people have shown interest in doing some markov chain stuff in neo4j. here's an example unmanaged extension for some markov operations.

### usage example:

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
  -d '{"lookup":"MATCH n WHERE n.name={lookup} RETURN id(n)", "start":"Rain", "end":"NoRain", "length":4}' \
   http://localhost:7474/markov
```

### configuration

Add this line to the end of your neo4j-server.properties file:
`org.neo4j.server.thirdparty_jaxrs_classes=markov=/markov`

Copy from dist `markov_2.10-0.1.jar`, `lift-json_2.10-2.5.jar` and `paranamer-2.4.1.jar` into the `lib/` folder in your neo4j server installation.
