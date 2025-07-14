window.addEventListener("DOMContentLoaded", () => {

    hideLoading();

    const input = document.getElementById("id_input_url");
    input.value = '';

    const errorMsg = localStorage.getItem('downloadError');

    if (errorMsg) {

        alert("❌ ERROR: " + errorMsg);
        localStorage.removeItem('downloadError');

    }

});


/**
 * Downloads the media given a form and a formData object.
 * 
 * @param {HTMLFormElement} form - The form containing the URL and format of the media to download.
 * @param {FormData} formData - The formData object containing the URL and format of the media to download.
 * 
 * @throws {Error} - If there is an error while downloading the media.
 */
function downloadMedia(form, formData) {

  responseHeader = null;

  setTimeout(() => {
    
    fetch(form.action, {

      method: 'POST',
      body: formData,
      
    })

    .then(async response => {

      responseHeader = response.headers.get('X-Error');

      if (!response.ok) {

        throw new Error("HTTP error: " + response.status);

      }

      const disposition = response.headers.get('Content-Disposition');
      let filename = 'download.mp4';

      if (disposition && disposition.includes('filename=')) {

        const match = disposition.match(/filename="?([^"]+)"?/);

        if (match && match[1]) {

          filename = match[1];

        }

      }

      const blob = await response.blob();
        return ({ blob, filename });

    })

    .then(({ blob, filename }) => {

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');

      a.href = url;
      a.download = filename;

      document.body.appendChild(a);

      a.click();
      a.remove();

      window.URL.revokeObjectURL(url);

      window.location.href = HOME_URL;

    })
    .catch(error => {

      console.error('Error downloading:', error);

      const errorHeader = responseHeader;

      if (errorHeader && errorHeader != null) {

        localStorage.setItem('downloadError', errorHeader);

      } else {

        localStorage.setItem('downloadError', 'There was an error while trying to download the media.');
      
      }

      window.location.href = HOME_URL;

    });

  }, 2000);
  
}

document.getElementById('download-form').addEventListener('submit', function(e) {
    
  e.preventDefault();

  const form = e.target;
  const formData = new FormData(form);

  showLoading();

  downloadMedia(form, formData);

});