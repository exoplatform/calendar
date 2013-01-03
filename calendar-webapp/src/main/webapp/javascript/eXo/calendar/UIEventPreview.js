function UIEventPreview() {
  this.hiddenThumbnail = null;
}

/**
 * captures onclick on the thumbnail div, 
 * display the thumbnail in the image preview 
 * display the hidden thumbnail
 * hides the thumbnail
 **/
UIEventPreview.prototype.showImagePreview = function(thumbnail) { 
  // show the hidden thumbnail 
  if (this.hiddenThumbnail !== null) {  gj(this.hiddenThumbnail).parent('div.imageThumbnail').show(); }
  // hide the clicked thumbnail
  gj(thumbnail).parent('div.imageThumbnail').hide();
  this.hiddenThumbnail = thumbnail;
  gj('img.imagePreview').attr('src', gj(thumbnail).attr('originalsrc'));
  // set download link for image preview
  gj('a#downloadImage').attr('href', gj(thumbnail).attr('downloadlink')); 
  gj('img.imagePreview').show();
  gj('img.closeButton').css('display', 'inline');	
};

/**
 * captures close action on image preview:
 * hide the preview, hide the button and display the hidden thumbnail 
 **/
UIEventPreview.prototype.closeImagePreview = function(closeButton) {
  gj('img.imagePreview').hide();
  gj(closeButton).hide();
  gj(this.hiddenThumbnail).parent('div.imageThumbnail').show();	
};

/**
 * captures click action on view icon and passes it to the near thumbnail
 **/
UIEventPreview.prototype.clickOnViewIcon = function(viewIcon) {
  // find the nearby thumbnail
  var thumbnail = gj(viewIcon).parent('div.imageThumbnail').children('img.thumbnail');
  this.showImagePreview(thumbnail);
}

UIEventPreview.prototype.hoverInThumbnail = function(thumbnailParent) {
  gj(thumbnailParent).css('background', 'rgba(0,0,0,0.2)');
}

UIEventPreview.prototype.hoverOutThumbnail = function(thumbnailParent) {
  gj(thumbnailParent).css('background', 'none');
}

_module.UIEventPreview = new UIEventPreview();
eXo.calendar.UIEventPreview = _module.UIEventPreview;