(function() {

var QuickSortObject = {
  processArray : false,
  desc : false,
  compareFunction : false,
  compareArgs : false,
  
  /**
  *
  * @param {Array} array
  * @param {Boolean} desc
  * @param {Function} compareFunction
  * @param {Object | Array} compareArgs
  */
 doSort : function(array, desc, compareFunction, compareArgs) {
   this.processArray = array;
   this.desc = desc;
   this.compareFunction = compareFunction;
   this.compareArgs = compareArgs;
   this.qSortRecursive(0, this.processArray.length);
 },

 /**
  *
  * @param {Integer} x
  * @param {Integer} y
  */
 swap : function(x, y) {
   if (this.processArray) {
     var tmp = this.processArray[x];
     this.processArray[x] = this.processArray[y];
     this.processArray[y] = tmp;
   }
 },

 /**
  *
  * @param {Integer} begin
  * @param {Integer} end
  * @param {Integer} pivotIndex
  */
 qSortRecursive : function(begin, end) {
   if (!this.processArray || begin >= end - 1) 
     return;
   var pivotIndex = begin + Math.floor(Math.random() * (end - begin - 1));
   var partionIndex = this.partitionProcess(begin, end, pivotIndex);
   this.qSortRecursive(begin, partionIndex);
   this.qSortRecursive(partionIndex + 1, end);
 },

 /**
  *
  * @param {Integer} begin
  * @param {Integer} end
  * @param {Integer} pivotIndex
  */
 partitionProcess : function(begin, end, pivotIndex) {
   var pivotValue = this.processArray[pivotIndex];
   this.swap(pivotIndex, end - 1);
   var scanIndex = begin;
   for (var i = begin; i < end - 1; i++) {
     if (typeof(this.compareFunction) == 'function') {
       if (!this.desc && this.compareFunction(this.processArray[i], pivotValue, this.compareArgs) <= 0) {
         this.swap(i, scanIndex);
         scanIndex++;
         continue;
       }
       else 
         if (this.desc && this.compareFunction(this.processArray[i], pivotValue, this.compareArgs) > 0) {
           this.swap(i, scanIndex);
           scanIndex++;
           continue;
         }
     }
     else {
       if (!this.desc && this.processArray[i] <= pivotValue) {
         this.swap(i, scanIndex);
         scanIndex++;
         continue;
       }
       else 
         if (this.desc && this.processArray[i] > pivotValue) {
           this.swap(i, scanIndex);
           scanIndex++;
           continue;
         }
     }
   }
   this.swap(end - 1, scanIndex);
   return scanIndex;
 }
}

return QuickSortObject;
})();