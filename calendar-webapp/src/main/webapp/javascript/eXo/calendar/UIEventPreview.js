(function(gj) {
	
var UIEventPreview = {
  hiddenThumbnail : null,
  
  /**
   * captures onclick on the thumbnail image, 
   * display the thumbnail in the image preview 
   * display the hidden thumbnail
   * hides the clicked thumbnail
   **/
  showImagePreview : function(thumbnail) { 
    // show the hidden thumbnail 
    if (UIEventPreview.hiddenThumbnail !== null) {  gj(UIEventPreview.hiddenThumbnail).parent('.thumbnailContainer').show(); }
    // hide the clicked thumbnail
    gj(thumbnail).parent('.thumbnailContainer').hide();
    UIEventPreview.hiddenThumbnail = thumbnail;
    gj('#imagePreview').attr('src', gj(thumbnail).attr('originalsrc'));
    // center the image 
    var marginLeft = Math.round( (gj('#downloadImage').parent('#imagePreviewContainer').parent('td').attr('width') - gj(thumbnail).attr('previewWidth')) / 2);
    gj('#downloadImage').parent('#imagePreviewContainer').css('margin-left', marginLeft + "px");
    // set download link for image preview
    gj('#downloadImage').attr('href', gj(thumbnail).attr('downloadlink'));
    gj('#imagePreviewContainer').show();
    gj('#imagePreview').show();
    gj('#closeButton').css('display', 'inline');  
  },

  /**
   * captures close action on image preview:
   * hide the preview, hide the button and display the hidden thumbnail 
   **/
  closeImagePreview : function(closeButton) {
    gj('#imagePreviewContainer').hide();
    gj('#imagePreview').hide();
    gj(closeButton).hide();
    gj(UIEventPreview.hiddenThumbnail).parent('.thumbnailContainer').show();  
  },

  /**
   * captures onclick on view icon container and passes it to the sibling thumbnail
   **/
  clickOnViewIconContainer : function(viewIconContainer) {
    var thumbnail = gj(viewIconContainer).parent('.thumbnailContainer').children('.thumbnail');
    UIEventPreview.showImagePreview(thumbnail);
  }
};

return UIEventPreview;
})(gj);