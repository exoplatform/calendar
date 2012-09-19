/**
 * @author uocnb
 */
//TODO : fix bug masklayer don't scroll with browser scrollbar(when show picture in cs and ks), remove this method when portal team fix it./
function MaskLayerControl() {
}

MaskLayerControl.prototype.init = function(root){
  root = (typeof(root) == 'string') ? document.getElementById(root) : root ;
  var nodeList = gj(root).find('span.ViewDownloadIcon') ;
  for (var i=0; i<nodeList.length; i++) {
    var linkNode = nodeList[i].getElementsByTagName('a')[0] ;
    linkNode.onclick = this.showPictureWrapper ;
  }
} ;

MaskLayerControl.prototype.showPictureWrapper = function() {
  _module.MaskLayerControl.showPicture(this) ;
  return false ;
} ;

/**
 * 
 * @param {Element} node
 */
MaskLayerControl.prototype.showPicture = function(node) {
  var attachmentContent = gj(node).parents('.AttachmentContent')[0]; 
  var imgSrcNode = gj(attachmentContent).find('img.AttachmentFile')[0] ;
	this.isMail = gj(node).parents(".UIMailPortlet")[0];
	if(!document.getElementById("UIPictutreContainer")){		
	  var containerNode = document.createElement('div') ;
		containerNode.id = "UIPictutreContainer";
	  with (containerNode.style) {
			position = "absolute";
			top = "0px";
	    width = '100%' ;
	    height = '100%' ;
	    textAlign = 'center' ;
	  }
	  containerNode.setAttribute('title', 'Click to close') ;
	  containerNode.onclick = this.hidePicture ;
		document.getElementById("UIPortalApplication").appendChild(containerNode)
	}else containerNode = document.getElementById("UIPictutreContainer");
	var imgSize = this.getImageSize(imgSrcNode);
	var windowHeight = document.documentElement.clientHeight;
	var windowWidth = document.documentElement.clientWidth;
	var marginTop = (windowHeight < parseInt(imgSize.height))?0:parseInt((windowHeight - parseInt(imgSize.height))/2);
	var imgHeight = (windowHeight < parseInt(imgSize.height))?windowHeight + "px":"auto";
	var imgWidth = (windowWidth < parseInt(imgSize.width))?windowWidth + "px":"auto";
	var imageNode = "<img src='" + imgSrcNode.src +"' style='height:" + imgHeight + ";width:"+ imgWidth +";margin-top:" + marginTop + "px;' alt='Click to close'/>";
  containerNode.innerHTML = imageNode;
  var maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication', containerNode, 30, 'CENTER') ;
	this.scrollHandler();	
} ;

MaskLayerControl.prototype.scrollHandler = function() {
	if(_module.MaskLayerControl.isMail) {
		eXo.core.UIMaskLayer.object.style.top = document.getElementById("MaskLayer").offsetTop + "px" ;
		_module.MaskLayerControl.timer = setTimeout(_module.MaskLayerControl.scrollHandler,1);
		return ;
	}
  eXo.core.UIMaskLayer.object.style.top = document.getElementById("MaskLayer").offsetTop + "px" ;
	_module.MaskLayerControl.timer = setTimeout(_module.MaskLayerControl.scrollHandler,1);
} ;

MaskLayerControl.prototype.hidePicture = function() {
  eXo.core.Browser.onScrollCallback.remove('MaskLayerControl') ;
  var maskContent = eXo.core.UIMaskLayer.object ;
  var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer") ;
  if (maskContent) maskContent.parentNode.removeChild(maskContent) ;
  if (maskNode) maskNode.parentNode.removeChild(maskNode) ;
	clearTimeout(_module.MaskLayerControl.timer);
	delete _module.MaskLayerControl.timer;
} ;

MaskLayerControl.prototype.getImageSize = function(img) {
	var imgNode = new Image();
	imgNode.src = img.src;
	return {"height":imgNode.height,"width":imgNode.width};
};

if (!eXo.cs) eXo.cs = {} ;

//eXo.cs.MaskLayerControl = new MaskLayerControl() ;
_module.MaskLayerControl = new MaskLayerControl() ;
