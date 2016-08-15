//Created by Marc Selwan (marc.selwan@datastax.com) and Shaunak Das at DataStax

config create_schema: false, load_new: false, load_threads: 3

productData = '/path/to/meta.json.gz'
reviewData = '/path/to/reviews.json.gz'
questionData = '/path/to/qa.json.gz'

question_data = File.json(questionData).gzip()

meta_data = File.json(productData).gzip().transform{
	it['key'] = [];
	if (it.containsKey("asin")){
		it['key'].add(['asin': it['asin']]);
	}
	if (it.containsKey("related")){
		it['also_bought'] = it['related']['also_bought'];
		it['also_viewed'] = it['related']['also_viewed'];
		it['buy_after_viewing'] = it['related']['buy_after_viewing'];
	}
	it['SalesRank'] = [];
	if (it.containsKey("salesRank"))
		for (key in it['salesRank'].keySet())
			it['SalesRank'].add(['category': key, 'rank': it['salesRank'][key]]);
	it['Categories'] = []
	if (it.containsKey("categories"))
		for (subarray in it["categories"])
			for (element in subarray)
				it['Categories'].add(element);
	it
}

review_data = File.json(reviewData).gzip().transform{
    it['edge_keys'] = []
    it['edge_keys'].add(['asin': it['asin'],
                         'overall': it['overall'],
                         //'helpful': it['helpful'], <- problem field
                         'reviewText': it['reviewText'],
                         'summary': it['summary'],
                         'reviewTime': it['reviewTime'],
												 'reviewerID': it['reviewerID'],
                         'unixReviewTime': it['unixReviewTime']
                        ]);
    it
	}



//create customer vertexLabel
customerV = {
    label "customer"
    key "reviewerID"


		//customer -(customer_reviewed)-> product

    outE "edge_keys", "customer_reviewed", {
        vertex "asin", {
           label "product"
           key "asin"
					 //ignore "reviewText"
					 //ignore "reviewerID"
        }
    }

		//ignore "asin" //asin is the link between customer to product. Think more like an invoice
    ignore "helpful"
		ignore "overall"
		ignore "reviewText"
		ignore "reviewTime"
		ignore "summary"
		ignore "unixReviewTime"

}

// create product VertexLabel
productV = {
    label "product"
    key "asin"
    // product -(viewed with)-> Item edge
    outV "also_viewed", "viewed_with", {
        label "product"
        key "asin"
    }
    // product -(purchased with)-> Item edge
    outV "also_bought", "purchased_with", {
        label "product"
        key "asin"
    }
    // product -(bought after viewing)-> Item edge
    outV "buy_after_viewing", "bought_after_viewing", {
        label "product"
        key "asin"
    }
    // product -(belongs_in_category)-> category edge
    outV "Categories", "belongs_in_category", {
        label "category"
        key "categories"
    }

    // product -(has_salesRank)-> category edge
    outE "SalesRank", "has_salesRank", {
        vertex "category", {
            label "category"
            key "categories"
        }
    }


		outE "key", "has_review", {
			    vertex "asin", {
				      label "review"
				      key "asin"
					    // ignore "related"
							// ignore "categories"
							// ignore "salesRank"
							// ignore "price"
							// ignore "title"
							// ignore "rank"
							// ignore "brand"
							// ignore "imgUrl"
				}
		}


    // these keys can be ignored, since we did appropriate transform on original JSON
    ignore "related"
    ignore "categories"
    ignore "salesRank"
}

reviewV = {
    label "review"
    key "asin"

		// inV "edge_keys", "customer_made", {
		// 		label "review"
		// 		key "reviewerID"
		// }


		outE "edge_keys", "made_by", {
				vertex "reviewerID", {
					 label "customer"
					 key "reviewerID"
					 ignore "reviewText"
					 ignore "helpful"
					 ignore "overall"
					 ignore "reviewText"
					 ignore "reviewTime"
					 ignore "summary"
					 ignore "unixReviewTime"
				}
		}
	}

questionV = {
	  //load property keys in
    label "question"
		key "asin"
    inV "asin", "has_question",{
        label "product"
        key "asin"
    }
}


//time to actually load the data
load(review_data).asVertices(customerV)
load(review_data).asVertices(reviewV)
load(question_data).asVertices(questionV)
load(meta_data).asVertices(productV)
