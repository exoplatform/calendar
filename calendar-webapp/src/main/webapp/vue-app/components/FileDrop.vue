<template>
  <div class="fileDrop">
    <div class="dropZone">
      <label class="dropMsg" for="cal-attach-file">
        <i class="uiIcon attachFileIcon"></i> {{ $t('ExoEventForm.label.attachFile') }}
      </label>
      <input type="file" class="attachFile" id="cal-attach-file" :value="selectedFile"/>
    </div>
    <div class="uploadContainer">
      <div class="uploadError" v-show="error">
          {{ error }}
      </div>
      <div class="file" v-for="file in files" :key="file.name">
        <div class="info clearfix pull-left">
          <div class="fileNameLabel pull-left" data-toggle="tooltip" rel="tooltip" data-placement="top" :title="file.name">{{ file.name }}</div>
          <div class="fileSize pull-left">({{ file.size }})</div>
          <div class="removeFile" v-show="file.progress == 100">
            <a href="#" rel="tooltip" data-placement="top" :title="$t('ExoEventForm.btn.delete')" @click="deleteFile(file.name)">
              <i class="uiIconClose"></i>
            </a>
          </div>
        </div>
        <div class="progress progress-striped pull-left" v-show="file.progress < 100">
          <div class="bar" :style="{width: (file.progress * 2) + 'px'}">{{ file.progress }} %</div>
        </div>
        <div class="abortFile pull-right" v-show="file.progress < 100">
          <a href="#" rel="tooltip" data-placement="top" :title="$t('ExoEventForm.btn.cancel')" @click="abortUpload(file.name)">
            <i class="uiIconRemove"></i>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {calConstants} from '../calConstants.js';

export default {
  model: {
    prop: 'files',
    event: 'change'
  },
  props: {
    files: {
      type: Array,
      default: []
    }
  },
  data() {
    return {
      selectedFile: '',
      error: ''
    }
  },
  methods: {
    abortUpload(name) {
      this.deleteFile(name);
    },
    deleteFile(name) {
      const idx = this.files.findIndex(f => f.name == name);
      const file = this.files[idx];
      this.files.splice(idx, 1);
      this.$emit('change', this.files);

      fetch(`${calConstants.UPLOAD_API}?uploadId=${file.uploadId}&action=delete`, {
                  method: 'post',
                  credentials: 'include'
      });
    },
    setErrorCode(code, param) {
      this.error = this.$t(code, param);
      setTimeout(() => {
        this.error = '';
      }, 3000);
    },
    getFileSize(size) {
      if (size < (1024 * 1024) / 100) {
        size = Number(size / 1024).toFixed(2);
        return `${size} KB`;
      } else {
        size = Number(size / (1024 * 1024)).toFixed(2);
        return `${size} MB`;
      }
    },
    getFileName(name) {
      while (this.files.find(file => file.name == name)) {
        name = name + '_';
      }
      return name;
    }
  },
  mounted() {
    const MAX_RANDOM_NUMBER = 100000;
    const $dropzoneContainer = $(this.$el).find('.dropZone');
    const thiss = this;

    let uploadId = '';
    $dropzoneContainer.filedrop({
      fallback_id: 'cal-attach-file',  // an identifier of a standard file input element
      url: function() {
        const random = Math.round(Math.random() * MAX_RANDOM_NUMBER);
        const now = Date.now();
        uploadId = `${random}-${now}`;

        return `${calConstants.UPLOAD_API}?uploadId=${uploadId}&action=upload`;
      },  // upload handler, handles each file separately, can also be a function taking the file and returning a url
      paramname: 'userfile',          // POST parameter name used on serverside to reference file
      error: function(err, file) {
        switch (err) {
          case 'ErrorBrowserNotSupported':
          case 'BrowserNotSupported':
            thiss.setErrorCode('ExoEventForm.error.BrowserNotSupported');
            break;
          case 'ErrorTooManyFiles':
          case 'TooManyFiles':
            thiss.setErrorCode('ExoEventForm.error.TooManyFiles', [calConstants.MAX_UPLOAD_FILES]);
            break;
          case 'ErrorFileTooLarge':
          case 'FileTooLarge':
            thiss.setErrorCode('ExoEventForm.error.FileTooLarge', [calConstants.MAX_UPLOAD_SIZE]);
            break;
        }
      },
      allowedfiletypes: [],   // filetypes allowed by Content-Type.  Empty array means no restrictions
      maxfiles: calConstants.MAX_UPLOAD_FILES,
      maxfilesize: calConstants.MAX_UPLOAD_SIZE,    // max file size in MBs
      rename: thiss.getFileName,
      uploadStarted: function(i, file, len) {
        const n = thiss.getFileName(file.name);
        thiss.files.push({
          'uploadId': uploadId,
          'name': n,
          'size': thiss.getFileSize(file.size),
          'progress': 0,
          'file': file
        });
      },
      progressUpdated: function (i, file, progress) {
        thiss.files.find(f => f.file == file).progress = progress;
      },
      uploadFinished: function (i, file, response) {
        thiss.$emit('change', thiss.files);
      },
      beforeEach: function(file) {
        if (thiss.files.length == calConstants.MAX_UPLOAD_FILES) {
          thiss.setErrorCode('ExoEventForm.error.TooManyFiles', [calConstants.MAX_UPLOAD_FILES]);
          return false;
        }
      },
      afterAll: function() {
        thiss.selectedFile = '';
      }
    });
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
