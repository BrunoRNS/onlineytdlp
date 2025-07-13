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
 * Downloads the SPA website given in the form.
 *
 * @param {HTMLFormElement} form - The form containing the SPA website URL.
 * @param {FormData} formData - The form data containing the URL.
 *
 * @returns {Promise<void>} - Redirect to home page.
 *
 * @throws {Error} If there is an error while downloading the SPA website.
 */
function downloadSPA(form, formData) {

  setTimeout(() => {
    
    fetch(form.action, {

      method: 'POST',
      body: formData,
      
    })

    .then(async response => {

      if (!response.ok) {

        return response.text().then(html => {

            document.documentElement.innerHTML = html;

            throw new Error("Invalid Response");

        });

      }

      const disposition = response.headers.get('Content-Disposition');
      let filename = 'download.zip';

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

      localStorage.setItem('downloadError', 'There was an error while trying to download the spa website.');

      window.location.href = HOME_URL;

    });

  }, 5000);
  
}

document.getElementById('downloadForm').addEventListener('submit', function(e) {
    
  e.preventDefault();

  const form = e.target;
  const formData = new FormData(form);

  showLoading();

  downloadSPA(form, formData);

});