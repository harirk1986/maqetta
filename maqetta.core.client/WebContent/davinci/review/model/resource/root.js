define([
	"dojo/_base/declare",
	"davinci/model/resource/Resource",
	"davinci/review/model/resource/Folder",
	"davinci/Runtime",
	"dojo/Deferred"
], function(declare, Resource, reviewFolder, Runtime, Deferred) {

var root = declare(Resource, {

	constructor: function(args) {
		this.elementType = "ReviewRoot";
		this.name = "root";
		this.parent = null;
	},

	findFile: function(version, fileName) {
		var promise = new Deferred();
		this.getChildren(function(children) {
			var node = null;
			dojo.forEach(children, function(item) {
				if (item.timeStamp == version) {
					node = item;
				}
			});
	
			var result = null;
			if (node != null) {
				node.getChildren(function(children) {
					dojo.forEach(children, function(item) {
						if (item.name == fileName) { 
							result = item;
						}
					});
					promise.resolve(result);
				});
			}
		});
		return promise;
	},

	findVersion: function(designerId, version) {
		//Get the top-level nodes of the tree (e.g., the children)
		var children;
		this.getChildren(function(c) { children= c; }, true);

		//Look amongst the children for a match
		var foundVersion = null;
		dojo.some(children,function(item){
			if (item.designerId == designerId && item.timeStamp == version) {
				foundVersion = item;
				return true;
			}
			return false;
		});
		return foundVersion;
	},

	getChildren: function(onComplete,sync) {
		if (!this._isLoaded) {
			if (this._loading) {
				this._loading.push(onComplete);
				return;
			}
			this._loading = [];
			this._loading.push(onComplete);
			
			Runtime.serverJSONRequest({
				url:  "cmd/listVersions",
				sync:sync,
				load : dojo.hitch(this, function(responseObject, ioArgs) {
					this.children=[];
					for (var i=0; i<responseObject.length; i++) {
						var child = new reviewFolder(dojo.mixin({
							name:responseObject[i].versionTitle,
							parent:this
						},responseObject[i]));
						this.children.push(child);
					}
					this._isLoaded=true;
					dojo.forEach(this._loading,function(item) {
						(item)(this.children);
					},this);
					delete this._loading;
				})
			});
			return;
		}
		onComplete(this.children);
	},

	getPath: function() {
		return ".review/snapshot";
	}

});

return dojo.setObject("davinci.review.model.resource.root", new root());

});
     
