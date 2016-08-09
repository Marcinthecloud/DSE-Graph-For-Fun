//Define our propertyKeys
schema.propertyKey("summary").Text().single().create()
schema.propertyKey("reviewerID").Text().single().create()
schema.propertyKey("unixReviewTime").Int().single().create()
schema.propertyKey("title").Text().single().create()
schema.propertyKey("imUrl").Text().single().create()
schema.propertyKey("reviewerName").Text().single().create()
schema.propertyKey("price").Double().single().create()
schema.propertyKey("rank").Int().single().create()
schema.propertyKey("overall").Double().single().create()
schema.propertyKey("asin").Text().single().create()
schema.propertyKey("categories").Text().single().create()
schema.propertyKey("helpful").Int().single().create()
schema.propertyKey("reviewText").Text().single().create()
schema.propertyKey("reviewTime").Text().single().create()
schema.propertyKey("description").type(java.lang.String).single().create()
schema.propertyKey("brand").type(java.lang.String).single().create()


//Define our Vertexes

schema.vertexLabel("product").properties("title", "imUrl", "price", "asin", "brand", "description").create()
schema.vertexLabel("review").properties("unixReviewTime", "reviewerName", "overall", "asin", "helpful", "reviewText", "reviewTime", "summary", "reviewerID").create()
schema.vertexLabel("category").properties("categories").create()
schema.vertexLabel("customer").properties("reviewerID", "reviewerName", "asin").create()

//Define our edges

//customer -(customer_reviewed)-> product
schema.edgeLabel("customer_reviewed").multiple().properties("unixReviewTime", "reviewerName", "overall", "asin", "helpful", "reviewText", "reviewTime", "summary", "reviewerID").create()
schema.edgeLabel("customer_reviewed").connection("customer", "product").add()

//review -(made_by)-> customer
schema.edgeLabel('made_by').properties('asin').connection('review', 'customer').create()

//review -(belongs_to)-> product
schema.edgeLabel('belongs_to_product').properties('asin').connection('review', 'product').create()

//customer -(customer_made)-> review
schema.edgeLabel('customer_made').connection('customer', 'review').create()

//product -(belongs_in_category)-> category
schema.edgeLabel("belongs_in_category").multiple().create()
schema.edgeLabel("belongs_in_category").connection("product", "category").add()

//product -(purchased_with)-> product
schema.edgeLabel('purchased_with').connection('product', 'product').create()

//product -(viewed_with)-> product
schema.edgeLabel('viewed_with').connection('product', 'product').create()

//product -(has_salesRank)-> category
schema.edgeLabel("has_salesRank").multiple().create()
schema.edgeLabel("has_salesRank").connection("product", "category").add()
schema.edgeLabel('has_salesRank').properties('rank').add()

//product -(has_review)-> reivew
schema.edgeLabel("has_review").multiple().properties("asin").create()
schema.edgeLabel('has_review').connection('product', 'review').add()

//product -(bought_after_viewing)-> product
schema.edgeLabel('bought_after_viewing').connection('product', 'product').create()



//Define our MV indexes for performance
schema.vertexLabel("product").index("byasin").materialized().by("asin").add()
schema.vertexLabel("review").index("byasin").materialized().by("asin").add()
schema.vertexLabel("category").index("bycategories").materialized().by("categories").add()
schema.vertexLabel("review").index("byreviewerID").materialized().by("reviewerID").add()
schema.vertexLabel('customer').index('byreviewerID').materialized().by('reviewerID').add()


//Define our Search indexes for flexability
schema.vertexLabel('review').index('search').search().by('reviewText').asText().by('summary').asText().by('reviewerName').asString().add()
