<template>
  <div class="fileDrop">
    <div class="dropZone">
      <label class="dropMsg" for="cal-attach-file">
        <i class="uiIcon attachFileIcon"></i> {{ $t('ExoEventForm.label.attachFile') }}
      </label>
      <input id="cal-attach-file" :value="selectedFile" type="file" class="attachFile"/>
    </div>
    <div class="uploadContainer">
      <div v-show="error" class="uploadError">
        {{ error }}
      </div>
      <div v-for="file in files" :key="file.name" class="file clearfix">
        <div class="info">
          <div :title="file.name" class="fileNameLabel pull-left" data-toggle="tooltip" rel="tooltip" data-placement="top">{{ decodeURIComponent(file.name) }}</div>
          <div class="fileSize pull-left">({{ getFileSize(file.size) }})</div>
          <div v-show="file.progress == 100" class="removeFile">
            <a :title="$t('ExoEventForm.btn.delete')" href="#" rel="tooltip" data-placement="top" @click="deleteFile(file.name)">
              <i class="uiIconClose"></i>
            </a>
          </div>
        </div>
        <div v-show="file.progress < 100" class="progress progress-striped pull-left">
          <div :style="{width: (file.progress * 2) + 'px'}" class="bar">{{ file.progress }} %</div>
        </div>
        <div v-show="file.progress < 100" class="abortFile pull-right">
          <a :title="$t('ExoEventForm.btn.cancel')" href="#" rel="tooltip" data-placement="top" @click="abortUpload(file.name)">
            <i class="uiIconRemove"></i>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {calConstants} from '../calConstants.js';

const ERROR_SHOW_TIME = 3000;
export default {
  model: {
    prop: 'files',
    event: 'change'
  },
  props: {
    files: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      selectedFile: '',
      error: ''
    };
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
      error: function(err) {
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
      uploadStarted: function(i, file) {
        const n = thiss.getFileName(file.name);
        thiss.files.push({
          'uploadId': uploadId,
          'name': n,
          'size': file.size,
          'progress': 0,
          'file': file
        });
      },
      progressUpdated: function (i, file, progress) {
        thiss.files.find(f => f.file === file).progress = progress;
      },
      uploadFinished: function () {
        thiss.$emit('change', thiss.files);
      },
      beforeEach: function() {
        if (thiss.files.length === calConstants.MAX_UPLOAD_FILES) {
          thiss.setErrorCode('ExoEventForm.error.TooManyFiles', [calConstants.MAX_UPLOAD_FILES]);
          return false;
        }
      },
      afterAll: function() {
        thiss.selectedFile = '';
      }
    });
  },
  methods: {
    abortUpload(name) {
      this.deleteFile(name);
    },
    deleteFile(name) {
      const idx = this.files.findIndex(f => f.name === name);
      const file = this.files[idx];
      this.files.splice(idx, 1);
      this.$emit('change', this.files);

      if (file.uploadId) {
        fetch(`${calConstants.UPLOAD_API}?uploadId=${file.uploadId}&action=delete`, {
          method: 'post',
          credentials: 'include'
        });
      }
    },
    setErrorCode(code, param) {
      this.error = this.$t(code, param);
      setTimeout(() => {
        this.error = '';
      }, ERROR_SHOW_TIME);
    },
    getFileSize(size) {
      const kilobyte = 1024;
      const hundred = 100;
      const fixed = 2;
      if (size < kilobyte * kilobyte / hundred) {
        size = Number(size / kilobyte).toFixed(fixed);
        return `${size} KB`;
      } else {
        size = Number(size / (kilobyte * kilobyte)).toFixed(fixed);
        return `${size} MB`;
      }
    },
    getFileName(name) {
      const find = file => file.name === name;
      while (this.files.find(find)) {
        name = `${name}_`;
      }
      return name;
    }
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
