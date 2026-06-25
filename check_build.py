import urllib.request, json

url = "https://api.github.com/repos/mosondz-podacha/driver-app/actions/runs?per_page=2"
r = urllib.request.urlopen(url)
data = json.loads(r.read())

for run in data.get("workflow_runs", []):
    print(f"Status: {run['status']} | Conclusion: {run.get('conclusion','running')}")
    print(f"URL: {run['html_url']}")
    print()