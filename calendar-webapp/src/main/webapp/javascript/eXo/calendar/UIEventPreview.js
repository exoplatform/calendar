function UIEventPreview() {

}

UIEventPreview.prototype.showImagePreview = function(imgElement) {  
  gj('img.imagePreview').attr('src', gj(imgElement).attr('originalsrc'));
  gj('img.imagePreview').attr('downloadlink', gj(imgElement).attr('downloadlink'));
  gj('img.imagePreview').show();
  gj('img.closeButton').show();	
};

UIEventPreview.prototype.closeImagePreview = function(closeButton) {
  gj('img.imagePreview').hide();
  gj(closeButton).hide();	
}

UIEventPreview.prototype.downloadOriginalImage = function(imagePreview) {
  alert(gj(imagePreview).attr('downloadlink'));
  gj.ajax(gj(imagePreview).attr('downloadlink'));
}

_module.UIEventPreview = new UIEventPreview();
eXo.calendar.UIEventPreview = _module.UIEventPreview;