const baseUrl = "http://localhost:8080";

export const getAllBlogPosts = async () => {
  const response = await fetch(baseUrl + "/posts");
  if (!response.ok) {
    throw new Error("Failed to fetch");
  }
  const data = await response.json();
  return data;
};
