/**
 * @author uocnb
 */
function AdvancedTooltip() {
}

AdvancedTooltip.prototype.onLoad = function(root, tmpl) {
  if (!this.tooltipE) {
    this.tooltipE = document.createElement('div') ;
    with (this.tooltipE.style) {
      display = 'none' ;
      position = 'absolute' ;
      boder = 'ridge 2px red' ;
      padding = '10px' ;
      background = '#fff' ;
      opacity = '80' ;
      filter = 'alpha(opacity=0.8)' ;
    }
    document.body.appendChild(this.tooltipE) ;
  }
  this.init(root, tmpl) ;
} ;

/**
 * 
 * @param {Element|String} root
 * @param {Element|String} tmpl
 */
AdvancedTooltip.prototype.init = function(root, tmpl) {
  root = (typeof(root) == 'string') ? document.getElementById(root) : root ;
  this.tmpl = (typeof(tmpl) == 'string') ? document.getElementById(tmpl) : tmpl ;
  var nodeLst = root.getElementsByTagName('*') ;
  for (var i=0; i<nodeLst.length; i++) {
    if (nodeLst[i].getAttribute('advancedtooltip')) {
      nodeLst[i].onmousemove = this.showAdvancedTooltip ;
      nodeLst[i].onmouseout = this.hideAdvancedTooltip ;
    }
  }
  this.tooltipE.className = this.tmpl.className ;
} ;

/**
 * 
 * @param {String} template
 */
AdvancedTooltip.prototype.applyTemplate = function(template, nodeData) {
  var regx = /(\$[_a-z0-9]+)/gim ;
  var var4Rep = template.match(regx) ;
  for (var  i=0; i<var4Rep.length; i++) {
    var property = nodeData.getAttribute(var4Rep[i].replace(/\$/, '')) ;
    if (property) {
      template = template.replace(var4Rep[i], property) ;
    }
  }
  return template ;
} ;

AdvancedTooltip.prototype.showAdvancedTooltip = function(e) {
  e = e ? e : window.event ;
  return eXo.cs.AdvancedTooltip.show(e, this) ;
} ;

/**
 * 
 * @param {Event} e
 * @param {Element} element
 */
AdvancedTooltip.prototype.show = function(e, element) {
  var compiledTmpl = this.applyTemplate(this.tmpl.innerHTML, element) ;
  this.tooltipE.innerHTML = compiledTmpl ;
  this.tooltipE.style['top'] = eXo.core.Browser.findMouseRelativeY(document.body, e) + 2 + 'px' ;
  this.tooltipE.style['left'] = eXo.core.Browser.findMouseRelativeX(document.body, e) + 2 + 'px' ;
  if (this.tooltipE.style['display'] == 'none') {
    this.cancelShowTimeout() ;
    this.showTimeoutId = window.setTimeout("eXo.cs.AdvancedTooltip.tooltipE.style['display'] = 'block' ;", 2*1000)
  }
} ;

AdvancedTooltip.prototype.hideAdvancedTooltip = function(e){
  e = e ? e : window.event ;
  return eXo.cs.AdvancedTooltip.hide(e, this) ;
} ;

AdvancedTooltip.prototype.hide = function(e, element) {
  this.cancelShowTimeout() ;
  window.setTimeout("eXo.cs.AdvancedTooltip.tooltipE.style['display'] = 'none' ;", 0.1*1000) ;
} ;

AdvancedTooltip.prototype.cancelShowTimeout = function() {
  if (this.showTimeoutId) {
    window.clearTimeout(this.showTimeoutId) ;
    this.showTimeoutId = false ;
  }
} ;

eXo.cs.AdvancedTooltip = new AdvancedTooltip() ;
