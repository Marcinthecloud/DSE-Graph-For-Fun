# DSE Graph For Fun!
![Alt text](https://upload.wikimedia.org/wikipedia/en/d/d3/Datastax_Logo.png)

This demo is intended to help get you started with DSE Graph. It includes schemas, data, and mapper script for the DataStax Graph Loader. A *special* thanks to Shaunak Das for helping with the mapper_script.

###About the Data
The data in this Git is only a small taste of what you can load with this script. This contains musical instrument product metadata and the respective reviews.

Larger datasets can be found here: http://jmcauley.ucsd.edu/data/amazon/

Credit for the data goes to:

Image-based recommendations on styles and substitutes
J. McAuley, C. Targett, J. Shi, A. van den Hengel
SIGIR, 2015


Inferring networks of substitutable and complementary products
J. McAuley, R. Pandey, J. Leskovec
Knowledge Discovery and Data Mining, 2015


###Prerequisites
* [Learn some Graph](https://academy.datastax.com/courses/ds330-datastax-enterprise-graph) <- this will give you ideas on how to query this graph
* [DataStax Graph Loader](https://academy.datastax.com/downloads/download-drivers)
* [DataStax Enterprise 5.0 or greater](https://www.datastax.com/downloads)
* [DataStax Studio 1.0 or greater](https://www.datastax.com/downloads)
* [Download the data (too large for GitHub)](https://drive.google.com/folderview?id=0B2STJKKPFt84WF8xUThYV0FKU2s&usp=sharing)


###How-to:
1. Start DataStax Enterprise in graph mode mode
2. Start DataStax Studio (port 9091)
3. Edit ```data_mapper.groovy``` so that the paths for the two files = `'/path/to/this/directory/'`

###Next, we'll want to create the graph and schemas

#####In DataStax Studio create a new connection with a graph called 'product_graph'

#####Next, paste the schema from the `schema.groovy` file into a new gremlin box:

#####Click the `real-time` play button to execute. When it finishes, hit the `schema` button at the top right of Studo. It should look like:
Note, there's plenty of other connections we can make with this dataset. Feel free to explore and play around!


#####Now we're going to do a dry run of the data load to make sure everything works ok.


`graphloader /path/to/data_mapper.groovy -graph product_graph -address localhost -dryrun true`

If everything works, you'll see no errors and the output may show some suggested schemas. We're going to ignore those for now.

###Now we're going to load the data

`graphloader /path/to/data_mapper.groovy -graph product_graph -address localhost`


###Now let's play around. Remember that Studio truncates results to 1000 by default.
*Note - some of these are stupid and inefficient traversals. Traverse responsibly*


`g.V()`

Behold! Data!

Let's make it more interesting...

`g.V().outE('customer_reviewed')`

![Alt text](http://i.imgur.com/qHn7lBx.png)

We can now get more clever with our traversals. For example, "Do people who write awesome in their reviews actual rate things as such?"

`g.V().has('review','summary', Search.tokenRegex('awesome')).values('overall').mean()`

Spoiler...Yes they do! 

Using similar logic - which ones are getting reviewed the most?

`g.V().has('review','summary', Search.tokenRegex('awesome')).groupCount().by("asin")`

It looks like 'B000ULAP4U' is on top...what product is that?

![Alt text](http://i.imgur.com/zUODNfq.png)

![Alt text](http://i.imgur.com/Z1MgkWx.png)

The Audio Technica M50's - one of the most popular reference/studio headphones. I own 2 pairs myself.

*This graph has many more possibilities for edges and connections. Explore and have fun with the dataset!*
